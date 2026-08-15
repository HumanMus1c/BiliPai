package com.android.purebilibili.feature.home.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Structure assertions for [FloatingBottomBar].
 * Interactions must use [com.android.purebilibili.feature.home.components.miuix.DampedDragAnimation],
 * not design-system DampedDragAnimationState / horizontalDragGesture.
 */
class FloatingBottomBarStructureTest {

    @Test
    fun `public API exposes BiliPai layout knobs`() {
        val source = loadFloatingBottomBarSource()

        assertTrue(source.contains("val LocalFloatingBottomBarContentColor"))
        assertTrue(source.contains("val LocalFloatingBottomBarTabScale"))
        assertTrue(source.contains("class FloatingBottomBarColors("))
        assertTrue(source.contains("object FloatingBottomBarDefaults"))
        assertTrue(source.contains("enum class FloatingBottomBarMode"))
        assertTrue(source.contains("LiquidGlass,"))
        assertTrue(source.contains("Blur,"))
        assertTrue(source.contains("None"))
        assertTrue(source.contains("fun RowScope.FloatingBottomBarItem("))
        assertTrue(source.contains("fun FloatingBottomBar("))
        assertTrue(source.contains("shellHeight: Dp = FloatingBottomBarDefaultShellHeight"))
        assertTrue(source.contains("indicatorHeight: Dp = FloatingBottomBarIndicatorHeight"))
        assertTrue(source.contains("FloatingBottomBarDefaultShellHeight: Dp = 64.dp"))
        assertTrue(source.contains("FloatingBottomBarIndicatorHeight: Dp = 56.dp"))
        assertTrue(source.contains("FloatingBottomBarPressedScale: Float = 78f / 56f"))
    }

    @Test
    fun `three layer liquid glass structure is present`() {
        val source = loadFloatingBottomBarSource()
        val body = source.substringAfter("fun FloatingBottomBar(")

        assertTrue(body.contains(".dropShadow("))
        assertTrue(body.contains("vibrancy()"))
        assertTrue(body.contains("blur(4.dp.toPx(), 4.dp.toPx())"))
        assertTrue(body.contains("refractionHeight = 24.dp.toPx()"))
        assertTrue(body.contains("refractionAmount = 24.dp.toPx()"))
        assertTrue(body.contains("baseHighlight.copy(alpha = 0.75f)"))

        assertTrue(body.contains(".alpha(0f)"))
        assertTrue(body.contains(".layerBackdrop(tabsBackdrop)"))
        assertTrue(body.contains("rememberLayerBackdrop()"))

        assertTrue(body.contains("rememberCombinedBackdrop(backdrop, tabsBackdrop)"))
        assertTrue(body.contains("depthEffect = true"))
        assertTrue(body.contains("chromaticAberration = 0.5f"))
        assertTrue(body.contains("refractionHeight = 10.dp.toPx() * progress"))
        assertTrue(body.contains("refractionAmount = 14.dp.toPx() * progress"))
        assertTrue(body.contains(".innerShadow(shape = pillShape)"))
        assertTrue(body.contains("InnerShadow("))
        assertTrue(body.contains("radius = 8.dp * dampedDragAnimation.pressProgress"))
    }

    @Test
    fun `drag uses local DampedDragAnimation not design-system horizontalDragGesture`() {
        val source = loadFloatingBottomBarSource()
        val dragPort = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/miuix/DampedDragAnimation.kt"
        )
        val body = source.substringAfter("fun FloatingBottomBar(")
        val baseRow = body
            .substringAfter("CompositionLocalProvider(LocalFloatingBottomBarContentColor provides colors.contentColor)")
            .substringBefore("if (isLiquidGlassMode && backdrop != null)")
        val movingIndicator = body.substringAfter("if (tabWidthPx > 0f)")

        assertTrue(source.contains("import com.android.purebilibili.feature.home.components.miuix.DampedDragAnimation"))
        assertTrue(body.contains("DampedDragAnimation("))
        assertTrue(body.contains("pressedScale = FloatingBottomBarPressedScale"))
        assertTrue(body.contains("canDrag = { offset ->"))
        assertTrue(body.contains("onDragStarted = {}"))
        assertTrue(body.contains("onDragStopped = {"))
        assertTrue(body.contains("updateValue("))
        assertTrue(body.contains("snapshotFlow { selectedIndex().coerceIn(0, maxTabIndex) }"))
        assertTrue(body.contains("snapshotFlow { currentIndex }"))
        assertTrue(body.contains(".drop(1)"))
        assertTrue(body.contains("onSelected(index)"))
        assertFalse(baseRow.contains("interactiveHighlight.gestureModifier"))
        assertFalse(baseRow.contains(".then(dampedDragAnimation.modifier)"))
        assertTrue(movingIndicator.contains("interactiveHighlight?.gestureModifier"))
        assertTrue(movingIndicator.contains(".then(dampedDragAnimation.modifier)"))
        assertTrue(body.contains("offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f))"))
        // Must not fall back to BiliPai self-developed drag stack.
        assertFalse(source.contains("rememberDampedDragAnimationState"))
        assertFalse(source.contains("horizontalDragGesture"))
        assertFalse(source.contains("DampedDragTrackingMode"))

        assertTrue(dragPort.contains("spring(1f, 1000f, visibilityThreshold)"))
        assertTrue(dragPort.contains("spring(0.5f, 300f, visibilityThreshold * 10f)"))
        assertTrue(dragPort.contains("spring(1f, 1000f, 0.001f)"))
        assertTrue(dragPort.contains("spring(0.6f, 250f, 0.001f)"))
        assertTrue(dragPort.contains("spring(0.7f, 250f, 0.001f)"))
        assertTrue(dragPort.contains("inspectDragGestures("))
        assertTrue(dragPort.contains("val modifier: Modifier = Modifier.pointerInput(Unit)"))
        assertTrue(dragPort.contains("private var requestedValue = initialValue.coerceIn(valueRange)"))
        assertTrue(dragPort.contains("val targetValue: Float get() = requestedValue"))
        assertTrue(dragPort.contains("requestedValue = targetValue"))
        assertTrue(dragPort.contains("onDragStopped()"))
        assertFalse(dragPort.contains("onDragCancelled"))
    }

    @Test
    fun `gravity highlight rubber band and InteractiveHighlight are wired together`() {
        val source = loadFloatingBottomBarSource()

        assertTrue(source.contains("fun rememberGravityRotatedHighlight("))
        assertTrue(source.contains("rememberDeviceTilt()"))
        assertTrue(source.contains("LIGHT_REF_X = 0.5f"))
        assertTrue(source.contains("LIGHT_REF_Y = 0.7f"))
        assertTrue(source.contains("GRAVITY_DIR_THRESHOLD_SQ = 0.01f"))
        assertTrue(source.contains("extraDegrees = -45f"))
        assertTrue(source.contains("extraDegrees = 90f"))
        assertTrue(source.contains("val offsetAnimation = remember { Animatable(0f) }"))
        assertTrue(source.contains("rubberBandPx"))
        assertTrue(source.contains("EaseOut.transform(abs(fraction))"))
        assertTrue(source.contains("4.dp.toPx()"))
        assertTrue(source.contains("InteractiveHighlight("))
        assertTrue(source.contains("Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU"))
        assertTrue(source.contains("snapshotFlow { currentIndex }"))
        assertTrue(source.contains(".drop(1)"))
        assertTrue(source.contains("onSelected(index)"))
        assertFalse(source.contains("pendingUserSelectedIndex"))
    }

    @Test
    fun `item provides dual LocalContentColor and tab scale`() {
        val source = loadFloatingBottomBarSource()
        val item = source
            .substringAfter("fun RowScope.FloatingBottomBarItem(")
            .substringBefore("fun FloatingBottomBar(")

        assertTrue(item.contains("LocalFloatingBottomBarTabScale.current"))
        assertTrue(item.contains("LocalFloatingBottomBarContentColor.current"))
        assertTrue(item.contains("MiuixLocalContentColor provides contentColor"))
        assertTrue(item.contains("M3LocalContentColor provides contentColor"))
        assertTrue(item.contains("role = Role.Tab"))
        assertTrue(item.contains(".semantics {"))
        assertTrue(item.contains(".clickable("))
        assertTrue(item.contains("onClick = onClick"))
        assertFalse(item.contains(".onKeyEvent { event ->"))
        assertTrue(item.contains(".weight(1f)"))
    }

    @Test
    fun `selected indicator forwards taps without replacing drag handling`() {
        val source = loadFloatingBottomBarSource()
        val indicatorSource = source.substringAfter("if (tabWidthPx > 0f)")

        assertTrue(source.contains("onReselected: () -> Unit = {}"))
        assertTrue(indicatorSource.contains(".then(dampedDragAnimation.modifier)"))
        assertTrue(indicatorSource.contains("onClick = onReselected"))
        assertTrue(indicatorSource.contains(".clearAndSetSemantics {}"))
    }

    @Test
    fun `caller width constrains dock before intrinsic measurement`() {
        val source = loadFloatingBottomBarSource()
        val body = source.substringAfter("fun FloatingBottomBar(")

        assertTrue(body.contains("modifier = modifier.width(IntrinsicSize.Min)"))
        assertFalse(body.contains("Modifier.width(IntrinsicSize.Min).then(modifier)"))
    }

    @Test
    fun `no dual-render bridge stub remains`() {
        val source = loadFloatingBottomBarSource()

        assertFalse(source.contains("FloatingBottomBarHost("))
        assertFalse(source.contains("FloatingBottomBarBridge("))
        assertFalse(source.contains("结构占位"))
        assertFalse(source.contains("委托给 BiliPai 现有的 BiliPaiFloatingBottomBar"))
    }

    private fun loadFloatingBottomBarSource(): String =
        loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/FloatingBottomBar.kt")

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(File(path), File(normalizedPath)).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText().replace("\r\n", "\n")
    }
}
