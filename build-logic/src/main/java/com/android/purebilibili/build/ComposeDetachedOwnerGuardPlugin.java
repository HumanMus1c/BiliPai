package com.android.purebilibili.build;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Compose "detached-root owner" guard.
 *
 * <p>Upstream issue #880: during a fullscreen switch while a SharedTransition morph is still
 * animating, a LayoutNode is detached from its Owner yet is measured again in the same frame.
 * {@code androidx.compose.ui.node.LayoutNodeKt.requireOwner} then throws
 *
 * <pre>java.lang.IllegalStateException: LayoutNode should be attached to an owner</pre>
 *
 * <p>The throwing method is package-private library code, so the guard is applied as a bytecode
 * rewrite of exactly one call site: the null branch inside
 * {@code LayoutNodeKt.requireOwner(LayoutNode)}. Instead of falling through to
 * {@code throwIllegalStateExceptionForNullCheck}, that branch is diverted to
 * {@code DetachedOwnerFallback.ownerOrRethrow}, which returns the node's last known Owner or
 * rethrows the original error when none was ever seen.
 *
 * <p>Scope is deliberately narrow: only {@code androidx/compose/ui/node/LayoutNodeKt.class} is
 * rewritten, and only that single branch. Every other class stays byte-identical.
 *
 * <p>The AAR is resolved from the app's own declared dependencies and the patched copy is written
 * inside the project's build directory. Nothing is read from, or written to, the Gradle transform
 * cache or {@code ~/.gradle/caches}, which is what lets the guard run on a cold CI runner.
 */
public class ComposeDetachedOwnerGuardPlugin implements Plugin<Project> {

    private static final String TARGET_CLASS = "androidx/compose/ui/node/LayoutNodeKt.class";
    private static final String HELPER_OWNER = "com/android/purebilibili/build/DetachedOwnerFallback";
    private static final String HELPER_DESC =
            "(Landroidx/compose/ui/node/LayoutNode;Landroidx/compose/ui/node/Owner;)Landroidx/compose/ui/node/Owner;";

    public void apply(Project project) {
        // Deferred on purpose: this plugin is applied from the plugins {} block, before Android
        // configurations exist. Everything below runs at task-graph time, so the AAR is resolved
        // from the app's own declared dependencies rather than from a hardcoded version.
        project.getTasks().register("patchComposeDetachedOwnerGuard", PatchTask.class, task -> {
            task.setGroup("build");
            task.setDescription("Rewrite LayoutNodeKt.requireOwner so a detached-root measure no "
                    + "longer throws (#880).");
            task.getComposeUiAar().from(project.provider(() -> {
                Configuration detached = resolveUiAndroid(project);
                File aar = null;
                for (File f : detached.getFiles()) {
                    if (f.getName().endsWith(".aar")) { aar = f; break; }
                }
                if (aar == null) {
                    throw new org.gradle.api.GradleException(
                            "[ComposeDetachedOwnerGuard] androidx.compose.ui:ui-android did not resolve "
                                    + "to an AAR (got " + detached.getFiles() + "). Refusing to produce "
                                    + "a build without the #880 guard.");
                }
                return java.util.Collections.singleton(aar);
            }));
            task.getPatchedAar().set(project.getLayout().getBuildDirectory()
                    .file("cdog/ui-android-patched.aar"));
        });

        project.getTasks().matching(t -> t.getName().startsWith("compile")
                        || t.getName().contains("Kotlin"))
                .configureEach(t -> t.dependsOn("patchComposeDetachedOwnerGuard"));
    }

    /**
     * Resolves the Android {@code ui-android} AAR at the version the app actually compiles against.
     *
     * <p>The version is not hardcoded. {@code app/build.gradle.kts} declares
     * {@code androidx.compose.ui:ui} without a version and lets the Compose BOM supply it. A bare
     * {@code detachedConfiguration("androidx.compose.ui:ui")} has no BOM in scope and fails to
     * resolve at all, and the BOM's own declared version is not the answer either: it pins
     * {@code ui} to 1.12.1 while the real build resolves {@code ui} to 1.13.0-alpha01, so patching
     * the declared version would rewrite an AAR the compiler never sees.
     *
     * <p>So the version is read off the resolution graph of a runtime classpath the app itself
     * declares. The whole graph is walked, not just its root, because Compose's modules arrive
     * through the BOM's constraints.
     */
    private static Configuration resolveUiAndroid(Project project) {
        String version = resolvedUiVersion(project);
        project.getLogger().lifecycle(
                "[ComposeDetachedOwnerGuard] compose-ui version in use: " + version);
        Configuration aar = project.getConfigurations().detachedConfiguration(
                project.getDependencies().create("androidx.compose.ui:ui-android:" + version));
        aar.setTransitive(false);
        return aar;
    }

    /** Compose UI version as actually resolved by the app, or a hard failure. */
    private static String resolvedUiVersion(Project project) {
        String version = null;
        for (Configuration c : project.getConfigurations()) {
            String n = c.getName();
            if (!n.endsWith("RuntimeClasspath") || n.startsWith("androidTest")
                    || n.startsWith("test") || n.startsWith("benchmark")) {
                continue;
            }
            try {
                version = findUiVersion(c.getIncoming().getResolutionResult().getRoot());
            } catch (RuntimeException ignored) {
                // Some classpaths only resolve for specific variants; try the next one.
            }
            if (version != null) break;
        }
        if (version == null) {
            throw new org.gradle.api.GradleException(
                    "[ComposeDetachedOwnerGuard] could not read the resolved "
                            + "androidx.compose.ui:ui version from any runtime classpath, so the "
                            + "matching ui-android AAR cannot be located. The guard will not guess.");
        }
        return version;
    }

    /** Breadth-first search: Compose's modules hang off the BOM, not off the graph root. */
    private static String findUiVersion(
            org.gradle.api.artifacts.result.ResolvedComponentResult root) {
        java.util.Set<org.gradle.api.artifacts.result.ResolvedComponentResult> seen =
                new java.util.HashSet<>();
        java.util.ArrayDeque<org.gradle.api.artifacts.result.ResolvedComponentResult> queue =
                new java.util.ArrayDeque<>();
        queue.add(root);
        seen.add(root);
        while (!queue.isEmpty()) {
            for (org.gradle.api.artifacts.result.DependencyResult dr :
                    queue.poll().getDependencies()) {
                if (!(dr instanceof org.gradle.api.artifacts.result.ResolvedDependencyResult)) continue;
                org.gradle.api.artifacts.result.ResolvedComponentResult sel =
                        ((org.gradle.api.artifacts.result.ResolvedDependencyResult) dr).getSelected();
                if (!seen.add(sel)) continue;
                org.gradle.api.artifacts.component.ComponentIdentifier id = sel.getId();
                if (id instanceof org.gradle.api.artifacts.component.ModuleComponentIdentifier) {
                    org.gradle.api.artifacts.component.ModuleComponentIdentifier m =
                            (org.gradle.api.artifacts.component.ModuleComponentIdentifier) id;
                    if ("androidx.compose.ui".equals(m.getGroup()) && "ui".equals(m.getModule())) {
                        return m.getVersion();
                    }
                }
                queue.add(sel);
            }
        }
        return null;
    }

    /** Serializable task: AAR in, patched AAR out. Never mutates the resolving cache. */
    public abstract static class PatchTask extends org.gradle.api.DefaultTask {

        private final ConfigurableFileCollection composeUiAar =
                getProject().getObjects().fileCollection();
        private final org.gradle.api.file.RegularFileProperty patchedAar =
                getProject().getObjects().fileProperty();

        /** The resolved {@code ui-android} AAR. Classpath-relative because Gradle reuses the
         * original artifact file whenever a build cache entry is a hit. */
        @Classpath
        public ConfigurableFileCollection getComposeUiAar() {
            return composeUiAar;
        }

        @OutputFile
        public org.gradle.api.file.RegularFileProperty getPatchedAar() {
            return patchedAar;
        }

        @TaskAction
        public void run() throws IOException {
            File aar = null;
            for (File f : composeUiAar.getFiles()) {
                if (f.getName().endsWith(".aar")) { aar = f; break; }
            }
            if (aar == null) {
                throw new org.gradle.api.GradleException(
                        "[ComposeDetachedOwnerGuard] androidx.compose.ui:ui-android did not resolve to "
                                + "an AAR (got " + composeUiAar.getFiles() + "). Refusing to produce a "
                                + "build without the #880 guard.");
            }

            File out = patchedAar.get().getAsFile();
            if (!out.getParentFile().isDirectory() && !out.getParentFile().mkdirs()) {
                throw new org.gradle.api.GradleException(
                        "Cannot create " + out.getParentFile() + " for the patched AAR.");
            }

            int sites = patchAar(aar, out);
            if (sites == 0) {
                throw new org.gradle.api.GradleException(
                        "[ComposeDetachedOwnerGuard] " + aar + " has no "
                                + TARGET_CLASS + " call site, so nothing was patched. Failing rather "
                                + "than shipping an unprotected build.");
            }
            getLogger().lifecycle("[ComposeDetachedOwnerGuard] patched " + aar.getName()
                    + " -> " + out + " (" + sites + " call site)");
        }
    }

    /** Copies the AAR, rewriting {@code classes.jar} on the way through. Every entry is kept. */
    private static int patchAar(File aar, File out) throws IOException {
        int[] sites = {0};
        try (ZipFile zip = new ZipFile(aar);
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(out))) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                zos.putNextEntry(new ZipEntry(entry));
                if (!entry.isDirectory()) {
                    try (InputStream in = zip.getInputStream(entry)) {
                        if ("classes.jar".equals(entry.getName())) {
                            zos.write(rewriteClassesJar(readAll(in), sites));
                        } else {
                            copy(in, zos);
                        }
                    }
                }
                zos.closeEntry();
            }
        }
        return sites[0];
    }

    private static byte[] rewriteClassesJar(byte[] jarBytes, int[] sites) throws IOException {
        File tmp = File.createTempFile("cdog", ".jar");
        try {
            try (FileOutputStream fos = new FileOutputStream(tmp)) { fos.write(jarBytes); }
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            try (JarFile jar = new JarFile(tmp);
                 JarOutputStream jos = new JarOutputStream(result)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry e = entries.nextElement();
                    byte[] data = null;
                    if (!e.isDirectory()) {
                        try (InputStream in = jar.getInputStream(e)) { data = readAll(in); }
                        if (TARGET_CLASS.equals(e.getName())) {
                            data = rewriteClass(data, sites);
                        }
                    }
                    jos.putNextEntry(new JarEntry(e));
                    if (data != null) jos.write(data);
                    jos.closeEntry();
                }
            }
            return result.toByteArray();
        } finally {
            if (!tmp.delete()) tmp.deleteOnExit();
        }
    }

    /**
     * Redirects the single null-check throw inside requireOwner.
     *
     * <p>Kotlin compiles the null check to:
     * <pre>
     *   invokevirtual LayoutNode.getOwner$ui()
     *   ...
     *   ifnonnull L
     *   ldc  "LayoutNode should be attached to an owner"
     *   invokestatic InlineClassHelperKt.throwIllegalStateExceptionForNullCheck
     * L: ...
     * </pre>
     * The {@code invokestatic} is replaced by a call to the fallback plus an {@code ARETURN},
     * making the throw unreachable on that path.
     */
    private static byte[] rewriteClass(byte[] classBytes, int[] sites) {
        ClassReader reader = new ClassReader(classBytes);
        final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        final int[] patched = {0};

        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!"requireOwner".equals(name)) return mv;

                return new MethodVisitor(Opcodes.ASM9, mv) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String mName,
                                                String mDesc, boolean isInterface) {
                        if ("throwIllegalStateExceptionForNullCheck".equals(mName)) {
                            super.visitVarInsn(Opcodes.ALOAD, 0);
                            super.visitVarInsn(Opcodes.ALOAD, 1);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, HELPER_OWNER,
                                    "ownerOrRethrow", HELPER_DESC, false);
                            super.visitInsn(Opcodes.ARETURN);
                            patched[0]++;
                            return;
                        }
                        super.visitMethodInsn(opcode, owner, mName, mDesc, isInterface);
                    }
                };
            }
        }, 0);

        sites[0] += patched[0];
        return writer.toByteArray();
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copy(in, bos);
        return bos.toByteArray();
    }

    private static void copy(InputStream in, java.io.OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
    }
}
