package com.android.purebilibili.feature.space

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.skeleton.ContentSkeletonBlock
import com.android.purebilibili.core.ui.skeleton.rememberContentSkeletonBlockColor
import com.android.purebilibili.core.ui.skeleton.rememberContentSkeletonPulse
import com.android.purebilibili.core.util.LocalWindowSizeClass
import com.android.purebilibili.feature.home.resolveHomeFeedCardLayout

/** UP 空间首屏骨架：资料头、主标签和投稿网格均与真实 SpaceContent 同构。 */
@Composable
internal fun SpaceLoadingSkeleton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val settings by SettingsManager.getHomeSettings(context)
        .collectAsStateWithLifecycle(
            initialValue = HomeSettings(androidNativeLiquidGlassEnabled = false)
        )
    val columns = resolveSpaceContentGridColumnCount(
        widthDp = LocalConfiguration.current.screenWidthDp,
        fixedColumnCount = settings.gridColumnCount,
        cardWidthPreset = settings.homeFeedCardWidthPreset,
        widthSizeClass = LocalWindowSizeClass.current.widthSizeClass,
    )
    val cardLayout = resolveHomeFeedCardLayout(
        style = settings.homeFeedCardStyle,
        gridColumns = columns,
        widthSizeClass = LocalWindowSizeClass.current.widthSizeClass,
    )
    val pulse = rememberContentSkeletonPulse()
    val blockColor = rememberContentSkeletonBlockColor(pulse)

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = cardLayout.outerPaddingDp.dp,
            vertical = 8.dp,
        ),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(cardLayout.itemSpacingDp.dp),
        verticalArrangement = Arrangement.spacedBy(cardLayout.verticalItemSpacingDp.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(Modifier.fillMaxWidth()) {
                ContentSkeletonBlock(
                    blockColor,
                    Modifier.fillMaxWidth().aspectRatio(3.2f),
                    AppShapes.container(ContainerLevel.Card),
                )
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ContentSkeletonBlock(blockColor, Modifier.size(76.dp), CircleShape)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        ContentSkeletonBlock(blockColor, Modifier.fillMaxWidth(0.36f).height(20.dp))
                        ContentSkeletonBlock(blockColor, Modifier.fillMaxWidth(0.62f).height(13.dp))
                        ContentSkeletonBlock(blockColor, Modifier.fillMaxWidth(0.48f).height(13.dp))
                    }
                    ContentSkeletonBlock(
                        blockColor,
                        Modifier.width(88.dp).height(40.dp),
                        AppShapes.container(ContainerLevel.Pill),
                    )
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            ContentSkeletonBlock(
                blockColor,
                Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 12.dp),
                AppShapes.container(ContainerLevel.Pill),
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            ContentSkeletonBlock(blockColor, Modifier.width(92.dp).height(20.dp))
        }
        items(List(columns * 4) { it }) {
            Column(Modifier.fillMaxWidth()) {
                ContentSkeletonBlock(
                    blockColor,
                    Modifier.fillMaxWidth().aspectRatio(cardLayout.coverAspectRatio),
                    AppShapes.container(ContainerLevel.Card),
                )
                Spacer(Modifier.height(8.dp))
                ContentSkeletonBlock(blockColor, Modifier.fillMaxWidth(0.9f).height(15.dp))
                Spacer(Modifier.height(6.dp))
                ContentSkeletonBlock(blockColor, Modifier.fillMaxWidth(0.52f).height(12.dp))
            }
        }
    }
}
