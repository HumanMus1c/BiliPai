package com.android.purebilibili.core.plugin.skin

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Rovniced/bilibili-skin 仓库冻结快照的装扮目录索引。
 *
 * 由 [scripts/generate_rovniced_skin_catalog.sh] 一次性生成并内置在 assets 中。
 * 浏览页据此展示主题列表（预览图、名称、颜色、能力位），选中后按
 * [SkinCatalogEntry.packageZipUrl] / [packageUrlCdn] 下载并走现有
 * [UiSkinImportPackageResolver] 转换导入。
 */
@Serializable
data class SkinCatalog(
    val catalogVersion: Int,
    val sourceRepo: String,
    val sourceBranch: String,
    val frozen: Boolean = true,
    val themes: List<SkinCatalogEntry> = emptyList()
)

@Serializable
data class SkinCatalogEntry(
    val id: String,
    val name: String,
    val previewUrl: String,
    val packageZipUrl: String? = null,
    val packageUrlCdn: String? = null,
    val themeMetadataUrl: String? = null,
    val colorMode: String? = null,
    val color: String? = null,
    val colorSecondPage: String? = null,
    val tailColor: String? = null,
    val capabilities: SkinCatalogCapabilities = SkinCatalogCapabilities()
) {
    /** 优先用 GitHub raw https 包；回退官方 CDN（已由 normalizeSkinPackageUrl 升级为 https）。 */
    fun preferredPackageUrl(): String? = packageZipUrl ?: packageUrlCdn

    /** GitHub 目录中的主题元数据；旧版内置索引无需新增字段即可按 zip 同目录推导。 */
    fun resolvedThemeMetadataUrl(): String? = themeMetadataUrl ?: (packageZipUrl ?: previewUrl)
        .substringBeforeLast('/', missingDelimiterValue = "")
        .takeIf { it.isNotBlank() }
        ?.plus("/%E4%B8%AA%E6%80%A7%E8%A3%85%E6%89%AE.json")

    /**
     * 旧索引曾混入 GB18030 字节；decodeToString 会将无效序列替换为 U+FFFD。
     * 目录 id 来自 GitHub UTF-8 路径，可作为稳定且可读的显示名兜底。
     */
    val displayName: String
        get() = name.takeUnless { it.contains('\uFFFD') } ?: id

    val isDark: Boolean get() = colorMode?.equals("dark", ignoreCase = true) == true
}

@Serializable
data class SkinCatalogCapabilities(
    val bottomBarIcons: Boolean = false,
    val bottomBarTrim: Boolean = false,
    val profileBackground: Boolean = false,
    val profileVideo: Boolean = false,
    val topAtmosphere: Boolean = false,
    val topTabBackground: Boolean = false,
    val sideBackground: Boolean = false,
    val drawerBottomTrim: Boolean = false,
    val publishIcon: Boolean = false,
    val animatedIcons: Boolean = false,
) {
    /** 该主题可提供的能力位标签（用于浏览页卡片角标）。 */
    fun labels(): List<String> = buildList {
        if (bottomBarIcons) add("底栏图标")
        if (bottomBarTrim) add("底栏饰面")
        if (profileBackground) add("个人页背景")
        if (profileVideo) add("个人页动效")
        if (topAtmosphere) add("顶部氛围")
        if (topTabBackground) add("标签背景")
        if (sideBackground) add("侧栏背景")
        if (drawerBottomTrim) add("侧栏底饰")
        if (publishIcon) add("发布图标")
        if (animatedIcons) add("底栏动效")
    }

    val isEmpty: Boolean get() =
        !bottomBarIcons && !bottomBarTrim && !profileBackground && !profileVideo &&
            !topAtmosphere && !topTabBackground && !sideBackground &&
            !drawerBottomTrim && !publishIcon && !animatedIcons
}

object SkinCatalogLoader {
    private const val ASSET_NAME = "rovniced-skin-catalog.json"
    private val json = Json { ignoreUnknownKeys = true }

    fun load(context: Context): Result<SkinCatalog> = runCatching {
        context.assets.open(ASSET_NAME).use { stream ->
            json.decodeFromString(SkinCatalog.serializer(), stream.readBytes().decodeToString())
        }
    }

    fun loadOrDefault(context: Context): SkinCatalog =
        load(context).getOrDefault(SkinCatalog(catalogVersion = 0, sourceRepo = "", sourceBranch = ""))
}
