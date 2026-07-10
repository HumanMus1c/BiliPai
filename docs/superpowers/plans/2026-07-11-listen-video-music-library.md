# 听视频音乐资料页与液态播放器 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将收藏夹、视频合集和 UP 主投影为播放列表、专辑和歌手，加入可滑入的“听视频”顶层入口，并完善播放器液态分页栏与歌词对齐。

**Architecture:** 新增只读 `feature/audio/library` 领域与 ViewModel，通过 `FavoriteRepository` 构建内存索引；MainHost Pager 增加真实的 `LISTEN_VIDEO` 页，播放继续写入现有 `PlaylistManager` 并进入 `VideoDetail(startAudio = true)`。播放器复用 `BottomBarLiquidSegmentedControl`，歌词先在纯 Kotlin 中规范化时间轴，再由 Compose 渲染和滚动。

**Tech Stack:** Kotlin、Jetpack Compose、Navigation3、Coroutines/Flow、DataStore Preferences、Media3/ExoPlayer、现有 Backdrop/液态底栏组件、kotlin.test/JUnit。

## Global Constraints

- 不新增 Gradle 依赖。
- 不新增 Room 表或数据库迁移。
- 不新增播放 Service，不改变 AU 音频和视频音轨各自的播放器所有权。
- 所有资料映射均为只读，不修改 B 站收藏夹。
- 底栏最多五项，交互目标至少 48dp。
- 实时折射仅走现有安全策略；不安全时使用高对比半透明降级。
- 不执行 APK 打包、安装或 release smoke 任务。
- 每个可验证切片通过后提交并推送到当前 `main`。

---

### Task 1: 顶层“听视频”入口与默认迁移

**Files:**
- Modify: `app/src/main/java/com/android/purebilibili/navigation/ScreenRoutes.kt`
- Modify: `app/src/main/java/com/android/purebilibili/navigation3/BiliPaiNavKey.kt`
- Modify: `app/src/main/java/com/android/purebilibili/navigation3/BiliPaiNavKeyMappingPolicy.kt`
- Modify: `app/src/main/java/com/android/purebilibili/navigation3/BiliPaiNavEntryContentPolicy.kt`
- Modify: `app/src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt`
- Modify: `app/src/main/java/com/android/purebilibili/core/store/SettingsManager.kt`
- Modify: `app/src/main/java/com/android/purebilibili/core/store/navigation/NavigationSettingsStore.kt`
- Modify: `app/src/main/java/com/android/purebilibili/feature/settings/screen/BottomBarSettingsScreen.kt`
- Modify: `app/src/main/java/com/android/purebilibili/navigation/AppNavigation.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`
- Modify: `app/src/main/res/values-zh-rTW/strings.xml`
- Test: `app/src/test/java/com/android/purebilibili/navigation/ListenVideoNavigationPolicyTest.kt`
- Test: `app/src/test/java/com/android/purebilibili/core/store/AppNavigationSettingsMappingPolicyTest.kt`

**Interfaces:**
- Produces: `ScreenRoutes.ListenVideo`, `BiliPaiNavKey.ListenVideo`, `BottomNavItem.LISTEN_VIDEO`, `BiliPaiNavEntryContentRole.LISTEN_VIDEO`.
- Produces: `resolveListenVideoBottomTabMigration(order, visible, migrationComplete): BottomTabMigrationResult` and `NavigationSettingsStore.ensureListenVideoBottomTabMigration(context)`.

- [ ] **Step 1: 写导航和迁移失败测试**

```kotlin
@Test
fun listenVideoRoute_roundTripsAndOwnsTopLevelRole() {
    assertEquals("listen_video", BiliPaiNavKey.ListenVideo.toLegacyRoute())
    assertEquals(BiliPaiNavKey.ListenVideo, legacyRouteToBiliPaiNavKey("listen_video"))
    assertEquals(
        BiliPaiNavEntryContentRole.LISTEN_VIDEO,
        resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.ListenVideo)
    )
}

@Test
fun migration_insertsListenVideoBeforeProfileWhenCapacityRemains() {
    val result = resolveListenVideoBottomTabMigration(
        order = listOf("HOME", "DYNAMIC", "HISTORY", "PROFILE"),
        visible = setOf("HOME", "DYNAMIC", "HISTORY", "PROFILE"),
        migrationComplete = false
    )
    assertEquals(listOf("HOME", "DYNAMIC", "HISTORY", "LISTEN_VIDEO", "PROFILE"), result.order)
    assertTrue("LISTEN_VIDEO" in result.visible)
    assertTrue(result.markComplete)
}

@Test
fun migration_preservesExistingFiveItemCustomization() {
    val original = listOf("HOME", "STORY", "FAVORITE", "LIVE", "PROFILE")
    val result = resolveListenVideoBottomTabMigration(original, original.toSet(), false)
    assertEquals(original, result.order)
    assertEquals(original.toSet(), result.visible)
}
```

- [ ] **Step 2: 运行测试并确认缺少新路由和迁移函数**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.android.purebilibili.navigation.ListenVideoNavigationPolicyTest' --tests 'com.android.purebilibili.core.store.AppNavigationSettingsMappingPolicyTest' --no-daemon --no-configuration-cache --console=plain
```

Expected: FAIL，编译器报告 `ListenVideo`、`LISTEN_VIDEO` 或迁移函数不存在。

- [ ] **Step 3: 实现最小导航和迁移策略**

```kotlin
internal data class BottomTabMigrationResult(
    val order: List<String>,
    val visible: Set<String>,
    val markComplete: Boolean
)

internal fun resolveListenVideoBottomTabMigration(
    order: List<String>,
    visible: Set<String>,
    migrationComplete: Boolean
): BottomTabMigrationResult {
    if (migrationComplete || "LISTEN_VIDEO" in order || "LISTEN_VIDEO" in visible) {
        return BottomTabMigrationResult(order, visible, markComplete = !migrationComplete)
    }
    if (visible.size >= BOTTOM_BAR_MAX_VISIBLE_ITEMS) {
        return BottomTabMigrationResult(order, visible, markComplete = true)
    }
    val insertAt = order.indexOf("PROFILE").takeIf { it >= 0 } ?: order.size
    val migratedOrder = order.toMutableList().apply { add(insertAt, "LISTEN_VIDEO") }
    return BottomTabMigrationResult(migratedOrder, visible + "LISTEN_VIDEO", markComplete = true)
}
```

在 `NavigationSettingsStore.ensureListenVideoBottomTabMigration` 内一次 DataStore `edit` 同时写入顺序、可见集合和布尔迁移标记。默认值改为 `HOME,DYNAMIC,HISTORY,LISTEN_VIDEO,PROFILE`。AppNavigation 启动后用 `LaunchedEffect(Unit)` 调用该迁移。

- [ ] **Step 4: 接通 BottomNavItem、设置页图标/文案和 MainHost 键映射**

```kotlin
LISTEN_VIDEO(
    "听视频",
    R.string.bottom_nav_listen_video,
    R.string.bottom_nav_listen_video_desc,
    listOf("音乐"),
    { Icon(CupertinoIcons.Filled.MusicNote, contentDescription = null) },
    { Icon(CupertinoIcons.Outlined.MusicNote, contentDescription = null) },
    ScreenRoutes.ListenVideo.route
)
```

`bottomPagerNavKeyForItem` 将该项映射为 `BiliPaiNavKey.ListenVideo`；MD3 图标使用 Filled/Outlined LibraryMusic，设置页将其列为默认可见项。

- [ ] **Step 5: 运行导航与设置定向测试**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.android.purebilibili.navigation.ListenVideoNavigationPolicyTest' --tests 'com.android.purebilibili.navigation.AppTopLevelNavigationPolicyTest' --tests 'com.android.purebilibili.navigation3.BiliPaiNavEntryContentPolicyTest' --tests 'com.android.purebilibili.navigation3.BiliPaiNavKeyMappingPolicyTest' --tests 'com.android.purebilibili.core.store.AppNavigationSettingsMappingPolicyTest' --no-daemon --no-configuration-cache --console=plain
```

Expected: PASS；若现有 JS 插件 round-trip 独立失败，只记录为既有问题，不把它归因于本切片。

- [ ] **Step 6: 提交并推送**

```bash
git add app/src/main app/src/test
git commit -m "feat(audio): add listen video bottom tab"
git push origin main
```

---

### Task 2: 收藏夹、合集和 UP 主的纯 Kotlin 资料投影

**Files:**
- Create: `app/src/main/java/com/android/purebilibili/feature/audio/library/ListenVideoLibraryModels.kt`
- Create: `app/src/main/java/com/android/purebilibili/feature/audio/library/ListenVideoLibraryPolicy.kt`
- Test: `app/src/test/java/com/android/purebilibili/feature/audio/library/ListenVideoLibraryPolicyTest.kt`

**Interfaces:**
- Consumes: `FavFolder`, `FavoriteData`, `PlaylistItem`。
- Produces: `ListenVideoTrack`, `ListenVideoPlaylist`, `ListenVideoAlbum`, `ListenVideoArtist`, `ListenVideoPlaybackSelection`。
- Produces: `mapListenVideoPlaylists`, `mapListenVideoAlbums`, `mapListenVideoArtists`, `resolveListenVideoPlaybackSelection`。

- [ ] **Step 1: 写资料映射失败测试**

```kotlin
@Test
fun foldersCollectionsAndUppersMapToMusicEntities() {
    val folders = listOf(FavFolder(id = 10, title = "通勤", media_count = 2))
    val media = listOf(
        FavoriteData(
            id = 1,
            bvid = "BV1",
            title = "Song A",
            duration = 180,
            upper = Upper(mid = 7, name = "Artist", face = "face"),
            ugc = FavoriteUgc(first_cid = 11)
        ),
        FavoriteData(
            id = 20,
            type = 21,
            season_id = 30,
            title = "Album",
            media_count = 4,
            upper = Upper(mid = 7, name = "Artist")
        )
    )

    assertEquals(listOf("通勤"), mapListenVideoPlaylists(folders).map { it.title })
    assertEquals(listOf(30L), mapListenVideoAlbums(emptyList(), media).map { it.seasonId })
    assertEquals(listOf(7L), mapListenVideoArtists(media).map { it.mid })
}

@Test
fun playbackSelectionDeduplicatesBvidAndKeepsClickedStart() {
    val tracks = listOf(track("BV1"), track("BV2"), track("BV1"))
    val selection = resolveListenVideoPlaybackSelection(tracks, clickedBvid = "BV2")
    assertEquals(listOf("BV1", "BV2"), selection.items.map { it.bvid })
    assertEquals(1, selection.startIndex)
}
```

- [ ] **Step 2: 运行测试并确认领域类型缺失**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.android.purebilibili.feature.audio.library.ListenVideoLibraryPolicyTest' --no-daemon --no-configuration-cache --console=plain
```

Expected: FAIL，领域类型和映射函数未定义。

- [ ] **Step 3: 实现不可变模型和映射策略**

```kotlin
internal fun FavoriteData.toListenVideoTrackOrNull(): ListenVideoTrack? {
    val resolvedBvid = bvid.ifBlank { bv_id }
    val artist = upper ?: return null
    if (type == 21 || resolvedBvid.isBlank() || artist.mid <= 0L) return null
    return ListenVideoTrack(
        bvid = resolvedBvid,
        cid = ugc?.first_cid ?: 0L,
        title = title.ifBlank { resolvedBvid },
        coverUrl = cover,
        durationMs = duration.coerceAtLeast(0) * 1_000L,
        artistId = artist.mid,
        artistName = artist.name.ifBlank { "UP主${artist.mid}" },
        artistAvatarUrl = artist.face
    )
}

internal fun mapListenVideoArtists(resources: List<FavoriteData>): List<ListenVideoArtist> =
    resources.mapNotNull(FavoriteData::toListenVideoTrackOrNull)
        .distinctBy(ListenVideoTrack::bvid)
        .groupBy(ListenVideoTrack::artistId)
        .map { (mid, tracks) ->
            val first = tracks.first()
            ListenVideoArtist(mid, first.artistName, first.artistAvatarUrl, tracks)
        }
        .sortedBy { it.name.lowercase() }
```

专辑映射合并 `type == 21` 的收藏合集和资源，并按 `seasonId` 去重；播放选择过滤无效 BVID，生成 `PlaylistItem` 并用去重后的 BVID 解析起始索引。

- [ ] **Step 4: 运行领域测试**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.android.purebilibili.feature.audio.library.ListenVideoLibraryPolicyTest' --no-daemon --no-configuration-cache --console=plain
```

Expected: PASS。

- [ ] **Step 5: 提交并推送**

```bash
git add app/src/main/java/com/android/purebilibili/feature/audio/library app/src/test/java/com/android/purebilibili/feature/audio/library
git commit -m "feat(audio): map favorites to music library"
git push origin main
```

---

### Task 3: 可取消的资料索引与 ViewModel 状态

**Files:**
- Create: `app/src/main/java/com/android/purebilibili/feature/audio/library/ListenVideoLibraryDataSource.kt`
- Create: `app/src/main/java/com/android/purebilibili/feature/audio/viewmodel/ListenVideoViewModel.kt`
- Test: `app/src/test/java/com/android/purebilibili/feature/audio/library/ListenVideoLibraryLoaderTest.kt`

**Interfaces:**
- Produces: `ListenVideoLibraryDataSource`，封装收藏夹目录、合集目录、收藏夹分页和合集分页。
- Produces: `ListenVideoUiState` 和 `ListenVideoViewModel.refresh/selectSection/openPlaylist/openAlbum/openArtist/retryFailedIndex`。
- Consumes: Task 2 的领域模型和映射函数。

- [ ] **Step 1: 写部分失败、分页和取消失败测试**

```kotlin
@Test
fun loaderKeepsSuccessfulFoldersWhenOneFolderFails() = runTest {
    val source = FakeListenVideoDataSource(
        folders = listOf(folder(1), folder(2)),
        pages = mapOf(1L to Result.success(page(track("BV1"))), 2L to Result.failure(IOException("offline")))
    )
    val result = ListenVideoLibraryLoader(source, maxConcurrentFolders = 3).index(42L)
    assertEquals(listOf("BV1"), result.resources.map { it.bvid })
    assertEquals(setOf(2L), result.failedFolderIds)
}

@Test
fun newerIndexRequestCancelsOlderGeneration() = runTest {
    val gate = CompletableDeferred<Unit>()
    val source = BlockingFakeDataSource(gate)
    val viewModel = ListenVideoViewModel(source, currentMid = { 42L })
    viewModel.refresh()
    viewModel.refresh()
    gate.complete(Unit)
    advanceUntilIdle()
    assertEquals(2L, viewModel.uiState.value.generation)
}
```

- [ ] **Step 2: 运行测试并确认加载器缺失**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.android.purebilibili.feature.audio.library.ListenVideoLibraryLoaderTest' --no-daemon --no-configuration-cache --console=plain
```

Expected: FAIL，加载器、数据源和 ViewModel 状态未定义。

- [ ] **Step 3: 实现数据源和最多三个收藏夹并发的加载器**

```kotlin
internal interface ListenVideoLibraryDataSource {
    suspend fun ownedFolders(mid: Long): Result<List<FavFolder>>
    suspend fun collectedFolders(mid: Long, page: Int): Result<CollectedFoldersPage>
    suspend fun folderPage(mediaId: Long, page: Int): Result<FavoriteResourceData>
    suspend fun albumPage(seasonId: Long, page: Int): Result<FavoriteResourceData>
}

internal class ListenVideoLibraryLoader(
    private val source: ListenVideoLibraryDataSource,
    maxConcurrentFolders: Int = 3
) {
    private val semaphore = Semaphore(maxConcurrentFolders)

    suspend fun index(mid: Long): ListenVideoIndexResult = supervisorScope {
        val folders = source.ownedFolders(mid).getOrThrow()
        val results = folders.map { folder ->
            async { semaphore.withPermit { loadAllFolderPages(folder.id) } }
        }.awaitAll()
        ListenVideoIndexResult(
            resources = results.flatMap { it.getOrDefault(emptyList()) },
            failedFolderIds = folders.mapIndexedNotNull { index, folder ->
                folder.id.takeIf { results[index].isFailure }
            }.toSet()
        )
    }
}
```

每个文件夹从第 1 页读取，依据 `has_more` 继续，取消异常原样抛出；其他异常转为该文件夹失败。Bilibili 实现直接委托 `FavoriteRepository`。

- [ ] **Step 4: 实现不可变 UI 状态和 generation 防旧请求回写**

```kotlin
internal data class ListenVideoUiState(
    val generation: Long = 0L,
    val isLoggedIn: Boolean = true,
    val isLoading: Boolean = false,
    val isIndexing: Boolean = false,
    val playlists: List<ListenVideoPlaylist> = emptyList(),
    val albums: List<ListenVideoAlbum> = emptyList(),
    val artists: List<ListenVideoArtist> = emptyList(),
    val failedFolderIds: Set<Long> = emptySet(),
    val error: String? = null
)
```

刷新时取消旧 `Job`，增加 generation；只有 generation 仍匹配时更新 StateFlow。未登录直接发布 `isLoggedIn=false`。

- [ ] **Step 5: 运行加载器和映射测试**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.android.purebilibili.feature.audio.library.*' --no-daemon --no-configuration-cache --console=plain
```

Expected: PASS。

- [ ] **Step 6: 提交并推送**

```bash
git add app/src/main/java/com/android/purebilibili/feature/audio app/src/test/java/com/android/purebilibili/feature/audio
git commit -m "feat(audio): load listen video library"
git push origin main
```

---

### Task 4: “听视频”资料页和播放接线

**Files:**
- Create: `app/src/main/java/com/android/purebilibili/feature/audio/screen/ListenVideoScreen.kt`
- Create: `app/src/main/java/com/android/purebilibili/feature/audio/screen/ListenVideoLayoutPolicy.kt`
- Modify: `app/src/main/java/com/android/purebilibili/navigation/AppNavigation.kt`
- Test: `app/src/test/java/com/android/purebilibili/feature/audio/screen/ListenVideoLayoutPolicyTest.kt`
- Test: `app/src/test/java/com/android/purebilibili/navigation/ListenVideoNavigationStructureTest.kt`

**Interfaces:**
- Consumes: `ListenVideoUiState`、`BottomBarLiquidSegmentedControl`、Task 2 的播放选择。
- Produces: `ListenVideoScreen(state, onRefresh, onSectionSelected, onTrackSelected, onLogin)`。

- [ ] **Step 1: 写布局和导航结构失败测试**

```kotlin
@Test
fun layoutUsesOneColumnOnCompactAndGridOnWide() {
    assertEquals(ListenVideoLayout.COMPACT_LIST, resolveListenVideoLayout(599))
    assertEquals(ListenVideoLayout.WIDE_GRID, resolveListenVideoLayout(600))
}

@Test
fun mainHostRendersListenVideoAsPagerContent() {
    val source = loadSource("app/src/main/java/com/android/purebilibili/navigation/AppNavigation.kt")
    assertTrue(source.contains("BiliPaiNavEntryContentRole.LISTEN_VIDEO"))
    assertTrue(source.contains("ListenVideoScreen("))
    assertTrue(source.contains("startAudio = true"))
}
```

- [ ] **Step 2: 运行测试并确认新屏幕缺失**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.android.purebilibili.feature.audio.screen.ListenVideoLayoutPolicyTest' --tests 'com.android.purebilibili.navigation.ListenVideoNavigationStructureTest' --no-daemon --no-configuration-cache --console=plain
```

Expected: FAIL。

- [ ] **Step 3: 实现状态驱动资料页与三段 Pager**

```kotlin
@Composable
internal fun ListenVideoScreen(
    state: ListenVideoUiState,
    onRefresh: () -> Unit,
    onSectionSelected: (ListenVideoSection) -> Unit,
    onTrackSelected: (List<ListenVideoTrack>, String) -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = state.section.ordinal, pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val indicatorPosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
    Column(modifier.fillMaxSize().statusBarsPadding()) {
        ListenVideoHeader(state, onRefresh)
        BottomBarLiquidSegmentedControl(
            items = listOf("播放列表", "专辑", "歌手"),
            selectedIndex = pagerState.currentPage,
            onSelected = { scope.launch { pagerState.animateScrollToPage(it) } },
            indicatorPositionProvider = { indicatorPosition },
            forceLiquidChrome = true
        )
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            ListenVideoSectionContent(page, state, onTrackSelected, onLogin)
        }
    }
}
```

使用 `LaunchedEffect(pagerState.settledPage)` 通知 ViewModel；卡片和空态均使用至少 48dp 的可点击区域。紧凑一列、600dp 起网格。

- [ ] **Step 4: 接通 AppNavigation 和 PlaylistManager**

```kotlin
BiliPaiNavEntryContentRole.LISTEN_VIDEO -> {
    val libraryViewModel: ListenVideoViewModel = viewModel()
    val state by libraryViewModel.uiState.collectAsStateWithLifecycle()
    ListenVideoScreen(
        state = state,
        onRefresh = libraryViewModel::refresh,
        onSectionSelected = libraryViewModel::selectSection,
        onTrackSelected = { tracks, clickedBvid ->
            val selection = resolveListenVideoPlaybackSelection(tracks, clickedBvid)
            PlaylistManager.setExternalPlaylist(
                selection.items,
                selection.startIndex,
                ExternalPlaylistSource.FAVORITE
            )
            pushNavigation3Key(
                BiliPaiNavKey.VideoDetail(
                    bvid = clickedBvid,
                    startAudio = true,
                    sourceRoute = ScreenRoutes.ListenVideo.route
                )
            )
        },
        onLogin = { pushNavigation3Key(BiliPaiNavKey.Login) }
    )
}
```

- [ ] **Step 5: 运行资料页和 MainHost 定向测试**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.android.purebilibili.feature.audio.screen.ListenVideoLayoutPolicyTest' --tests 'com.android.purebilibili.navigation.ListenVideoNavigationStructureTest' --tests 'com.android.purebilibili.navigation.AppTopLevelNavigationPolicyTest' --no-daemon --no-configuration-cache --console=plain
```

Expected: PASS。

- [ ] **Step 6: 提交并推送**

```bash
git add app/src/main app/src/test
git commit -m "feat(audio): add listen video library screen"
git push origin main
```

---

### Task 5: 播放器液态分页栏和高对比降级

**Files:**
- Modify: `app/src/main/java/com/android/purebilibili/feature/audio/screen/MusicPlayerContent.kt`
- Modify: `app/src/main/java/com/android/purebilibili/feature/audio/screen/MusicPlayerLayoutPolicy.kt`
- Modify: `app/src/main/java/com/android/purebilibili/feature/audio/screen/MusicPlayerVisualPolicy.kt`
- Test: `app/src/test/java/com/android/purebilibili/feature/audio/screen/MusicPlayerLayoutPolicyTest.kt`
- Test: `app/src/test/java/com/android/purebilibili/feature/audio/screen/MusicPlayerVisualPolicyTest.kt`
- Test: `app/src/test/java/com/android/purebilibili/feature/audio/screen/MusicPlayerStructureTest.kt`

**Interfaces:**
- Consumes: `BottomBarLiquidSegmentedControl` 和紧凑播放器 Pager。
- Produces: `resolveMusicPagerIndicatorPosition(page, offsetFraction)`、`MusicGlassFallbackStyle`。

- [ ] **Step 1: 写连续指示器与降级失败测试**

```kotlin
@Test
fun pagerIndicatorFollowsContinuousSwipe() {
    assertEquals(0.35f, resolveMusicPagerIndicatorPosition(0, 0.35f))
    assertEquals(1f, resolveMusicPagerIndicatorPosition(1, 0.4f))
}

@Test
fun unsafeRendererKeepsVisibleHighContrastGlassFallback() {
    val style = resolveMusicGlassFallbackStyle(glassEnabled = false, darkBackground = true)
    assertTrue(style.surfaceAlphaPercent >= 46)
    assertTrue(style.borderAlphaPercent >= 22)
}
```

- [ ] **Step 2: 运行测试并确认策略缺失**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.android.purebilibili.feature.audio.screen.MusicPlayerVisualPolicyTest' --tests 'com.android.purebilibili.feature.audio.screen.MusicPlayerLayoutPolicyTest' --tests 'com.android.purebilibili.feature.audio.screen.MusicPlayerStructureTest' --no-daemon --no-configuration-cache --console=plain
```

Expected: FAIL。

- [ ] **Step 3: 在紧凑 Pager 上叠加同步液态分段栏**

```kotlin
val pagerState = rememberPagerState(pageCount = { 2 })
val scope = rememberCoroutineScope()
val indicatorPosition = resolveMusicPagerIndicatorPosition(
    pagerState.currentPage,
    pagerState.currentPageOffsetFraction
)
HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
    if (page == 0) PlayerPage(...) else LyricsPage(...)
}
BottomBarLiquidSegmentedControl(
    items = listOf("播放", "歌词"),
    selectedIndex = pagerState.currentPage,
    onSelected = { scope.launch { pagerState.animateScrollToPage(it) } },
    modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 10.dp),
    indicatorPositionProvider = { indicatorPosition },
    liquidGlassEffectsEnabled = liquidGlassEffectsEnabled,
    forceLiquidChrome = true,
    containerColorOverride = Color.Black.copy(alpha = fallback.surfaceAlphaPercent / 100f)
)
```

PlayerPage 和 LyricsPage 增加足够的底部 padding，避免遮挡。宽屏不显示分页控件，但使用同一高对比玻璃操作栏。

- [ ] **Step 4: 强化功能栏和非折射降级**

`musicGlassSurface` 的降级表面按策略使用至少 46% 的深色底、22% 白色边框和顶部高光；真实折射路径继续受安全策略控制。把队列、模式、视频、合集、定时和 PIP 放在一个有共同玻璃底的功能架内，每个按钮保持 48dp。

- [ ] **Step 5: 运行播放器定向测试**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.android.purebilibili.feature.audio.screen.*' --tests 'com.android.purebilibili.feature.video.screen.AudioMode*' --no-daemon --no-configuration-cache --console=plain
```

Expected: PASS。

- [ ] **Step 6: 提交并推送**

```bash
git add app/src/main/java/com/android/purebilibili/feature/audio/screen app/src/test/java/com/android/purebilibili/feature/audio/screen
git commit -m "feat(audio): add liquid player page selector"
git push origin main
```

---

### Task 6: 歌词规范化、活动区间和密度无关滚动

**Files:**
- Modify: `app/src/main/java/com/android/purebilibili/feature/audio/lyrics/LyricsParser.kt`
- Create: `app/src/main/java/com/android/purebilibili/feature/audio/lyrics/LyricsTimelinePolicy.kt`
- Modify: `app/src/main/java/com/android/purebilibili/feature/audio/screen/MusicPlayerVisualPolicy.kt`
- Modify: `app/src/main/java/com/android/purebilibili/feature/audio/screen/MusicPlayerContent.kt`
- Modify: `app/src/test/java/com/android/purebilibili/feature/audio/lyrics/LyricsParserTest.kt`
- Create: `app/src/test/java/com/android/purebilibili/feature/audio/lyrics/LyricsTimelinePolicyTest.kt`
- Modify: `app/src/test/java/com/android/purebilibili/feature/audio/screen/MusicPlayerVisualPolicyTest.kt`

**Interfaces:**
- Produces: `normalizeLyricTimeline(lines)`、`mergeAuxiliaryLyrics(primary, auxiliary, toleranceMs = 650)`、`resolveActiveLyricIndex(document, positionMs)`、`resolveLyricFocusScrollOffsetPx(viewportHeightPx, focusFraction)`。
- Replaces: `resolveCurrentLyricIndex` 和固定 `-160` 像素偏移。

- [ ] **Step 1: 写全部时间轴边界失败测试**

```kotlin
@Test
fun approximateTranslationMergesWithinSixHundredFiftyMilliseconds() {
    val document = parseSplLyrics(
        primary = "[00:10.00]Primary",
        translation = "[00:10.42]翻译"
    )
    assertEquals(listOf("翻译"), document.lines.single().translations)
}

@Test
fun duplicateAndSimultaneousLinesNormalizeDeterministically() {
    val document = parseSplLyrics(
        "[00:01.00]A\n[00:01.00]A\n[00:01.00]B\n[00:04.00]C"
    )
    assertEquals(listOf("A\nB", "C"), document.lines.map { it.text })
}

@Test
fun explicitEndCreatesInactiveInstrumentalGapAndLastLineExpires() {
    val document = parseSplLyrics(
        "[00:01.00]A<00:02.00>\n[00:10.00]B<00:12.00>"
    )
    assertEquals(0, resolveActiveLyricIndex(document, 1_500))
    assertEquals(-1, resolveActiveLyricIndex(document, 5_000))
    assertEquals(1, resolveActiveLyricIndex(document, 11_000))
    assertEquals(-1, resolveActiveLyricIndex(document, 12_500))
}

@Test
fun focusOffsetUsesViewportFractionInsteadOfRawDevicePixels() {
    assertEquals(-300, resolveLyricFocusScrollOffsetPx(1_000, 0.30f))
    assertEquals(-760, resolveLyricFocusScrollOffsetPx(2_000, 0.38f))
}
```

- [ ] **Step 2: 运行歌词测试并确认失败**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.android.purebilibili.feature.audio.lyrics.LyricsParserTest' --tests 'com.android.purebilibili.feature.audio.lyrics.LyricsTimelinePolicyTest' --tests 'com.android.purebilibili.feature.audio.screen.MusicPlayerVisualPolicyTest' --no-daemon --no-configuration-cache --console=plain
```

Expected: FAIL，近似翻译、规范化和活动区间行为尚未实现。

- [ ] **Step 3: 实现辅助歌词近邻匹配和时间轴规范化**

```kotlin
internal fun mergeAuxiliaryLyrics(
    primary: List<ParsedRawLine>,
    auxiliary: List<ParsedRawLine>,
    toleranceMs: Long = 650L
): Map<Long, List<String>> {
    val unused = primary.indices.toMutableSet()
    val merged = linkedMapOf<Long, MutableList<String>>()
    auxiliary.sortedBy { it.startTimeMs }.forEach { aux ->
        val target = unused.minWithOrNull(
            compareBy<Int> { abs(primary[it].startTimeMs - aux.startTimeMs) }
                .thenBy { primary[it].startTimeMs }
        )?.takeIf { abs(primary[it].startTimeMs - aux.startTimeMs) <= toleranceMs }
        if (target != null && aux.text.isNotBlank()) {
            merged.getOrPut(primary[target].startTimeMs) { mutableListOf() } += aux.text
            unused -= target
        }
    }
    return merged
}
```

主歌词规范化按时间分组、文本去重后以换行合并；保留显式结束信息，逐字片段裁剪为不重叠的递增区间。

- [ ] **Step 4: 实现无状态活动歌词二分解析**

```kotlin
internal fun resolveActiveLyricIndex(document: LyricDocument, positionMs: Long): Int {
    val position = positionMs - document.offsetMs
    var low = 0
    var high = document.lines.lastIndex
    var candidate = -1
    while (low <= high) {
        val middle = (low + high) ushr 1
        if (document.lines[middle].startTimeMs <= position) {
            candidate = middle
            low = middle + 1
        } else high = middle - 1
    }
    if (candidate < 0) return -1
    return candidate.takeIf { position < document.lines[it].endTimeMs } ?: -1
}
```

逐字高亮判断同时使用开始与结束时间。正负偏移只影响播放位置，不修改行时间。

- [ ] **Step 5: 替换固定像素滚动并加入手动滚动暂停**

使用 `LazyListState.layoutInfo.viewportSize.height` 和布局策略计算焦点偏移。通过 `snapshotFlow { listState.isScrollInProgress }` 区分用户拖动，在用户拖动后写入 `autoFollowResumeAtMs = now + 3_000`；歌词点击 seek 将其清零。减少动画时使用 `scrollToItem`，其他情况使用 `animateScrollToItem`。

- [ ] **Step 6: 运行所有歌词和视觉策略测试**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.android.purebilibili.feature.audio.lyrics.*' --tests 'com.android.purebilibili.feature.audio.screen.MusicPlayerVisualPolicyTest' --no-daemon --no-configuration-cache --console=plain
```

Expected: PASS。

- [ ] **Step 7: 提交并推送**

```bash
git add app/src/main/java/com/android/purebilibili/feature/audio app/src/test/java/com/android/purebilibili/feature/audio
git commit -m "fix(audio): normalize lyric alignment timeline"
git push origin main
```

---

### Task 7: 回归、编译和完成审计

**Files:**
- Modify only if a touched-feature regression requires a targeted correction.

**Interfaces:**
- Verifies all interfaces produced by Tasks 1–6.

- [ ] **Step 1: 运行新增功能测试集合**

```bash
./gradlew :app:testDebugUnitTest \
  --tests 'com.android.purebilibili.feature.audio.library.*' \
  --tests 'com.android.purebilibili.feature.audio.lyrics.*' \
  --tests 'com.android.purebilibili.feature.audio.screen.*' \
  --tests 'com.android.purebilibili.navigation.ListenVideo*' \
  --no-daemon --no-configuration-cache --console=plain
```

Expected: PASS。

- [ ] **Step 2: 运行既有播放、底栏和导航回归测试**

```bash
./gradlew :app:testDebugUnitTest \
  --tests 'com.android.purebilibili.feature.video.screen.AudioMode*' \
  --tests 'com.android.purebilibili.feature.video.player.MiniPlayer*' \
  --tests 'com.android.purebilibili.feature.video.player.PlaylistManager*' \
  --tests 'com.android.purebilibili.feature.home.components.BottomBar*' \
  --tests 'com.android.purebilibili.navigation.AppTopLevelNavigationPolicyTest' \
  --tests 'com.android.purebilibili.navigation3.BiliPaiNavEntryContentPolicyTest' \
  --no-daemon --no-configuration-cache --console=plain
```

Expected: PASS；已知且与本功能无关的 JS 插件 route 失败单独报告。

- [ ] **Step 3: 编译 Kotlin**

```bash
./gradlew :app:compileDebugKotlin --no-daemon --no-configuration-cache --console=plain
```

Expected: `BUILD SUCCESSFUL`。

- [ ] **Step 4: 检查设备并执行可用的窄测**

```bash
adb devices -l
```

若存在已连接设备，验证底栏滑入、三段资料页、播放队列、歌词快进/回退、暗色和横屏；若无设备，明确记录设备验证缺失，不执行安装任务。

- [ ] **Step 5: 审计最终差异与需求覆盖**

```bash
git status --short
git diff --check
git log --oneline -12
```

逐项对照设计规格：底栏入口、三类资料投影、指示器滑动、播放器玻璃底栏、歌词场景、回归测试和编译证据均必须存在。

- [ ] **Step 6: 提交任何最终定向修正并推送**

```bash
git add app/src/main app/src/test
git commit -m "test(audio): verify listen video experience"
git push origin main
```

若没有额外修正，不创建空提交。
