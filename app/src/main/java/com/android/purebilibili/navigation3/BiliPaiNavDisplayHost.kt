package com.android.purebilibili.navigation3

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.scene.SceneInfo
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.scene.rememberSceneState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.NavigationEventState
import androidx.navigationevent.compose.rememberNavigationEventState
import com.android.purebilibili.core.ui.ProvideAnimatedVisibilityScope
import com.android.purebilibili.core.ui.transition.LocalVideoCardSharedElementSourceRoute
import com.android.purebilibili.navigation3.predictiveback.BiliPaiPredictiveBackAnimationHandler
import com.android.purebilibili.navigation3.predictiveback.resolveBiliPaiPredictiveBackAnimationHandler
import kotlinx.coroutines.launch

@Composable
internal fun BiliPaiNavDisplayHost(
    backStack: List<BiliPaiNavKey>,
    cardTransitionEnabled: Boolean = true,
    sourceMetadata: BiliPaiNavSourceMetadata,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    visibleBottomBarRoutes: Set<String> = emptySet(),
    activeMainHostRoute: String? = null,
    content: @Composable (BiliPaiNavKey) -> Unit
) {
    val safeBackStack = remember(backStack) {
        backStack.ifEmpty { listOf(BiliPaiNavKey.MainHost) }
    }
    val application = LocalContext.current.applicationContext as Application
    var navigationEventState: NavigationEventState<SceneInfo<BiliPaiNavKey>>? = null
    val navigationScope = rememberCoroutineScope()
    val popRouteTransition = remember(cardTransitionEnabled, sourceMetadata, safeBackStack) {
        resolveBiliPaiNavDisplayPopRouteTransition(
            cardTransitionEnabled = cardTransitionEnabled,
            sourceMetadata = sourceMetadata,
            fromKey = safeBackStack.lastOrNull(),
            toKey = safeBackStack.getOrNull(safeBackStack.lastIndex - 1)
        )
    }
    val predictiveBackHandler: BiliPaiPredictiveBackAnimationHandler = remember(popRouteTransition) {
        resolveBiliPaiPredictiveBackAnimationHandler(popRouteTransition)
    }
    val performBack: () -> Unit = {
        navigationScope.launch {
            predictiveBackHandler.onBackPressed(
                transitionState = navigationEventState?.transitionState,
                currentPageKey = safeBackStack.lastOrNull(),
            )
            onBack()
        }
    }
    val scopedContent: @Composable (BiliPaiNavKey) -> Unit = remember(content, application) {
        { key ->
            ProvideAnimatedVisibilityScope(
                animatedVisibilityScope = LocalNavAnimatedContentScope.current
            ) {
                CompositionLocalProvider(
                    LocalVideoCardSharedElementSourceRoute provides key.toLegacyRoute()
                ) {
                    ProvideNavigation3ViewModelApplicationExtras(application) {
                        content(key)
                    }
                }
            }
        }
    }
    val entryProvider = remember(sourceMetadata, cardTransitionEnabled, visibleBottomBarRoutes, activeMainHostRoute, scopedContent) {
        biliPaiNavEntryProvider(
            sourceMetadata = sourceMetadata,
            cardTransitionEnabled = cardTransitionEnabled,
            visibleBottomBarRoutes = visibleBottomBarRoutes,
            activeMainHostRoute = activeMainHostRoute,
            content = scopedContent
        )
    }
    val entries = rememberDecoratedNavEntries(
        backStack = safeBackStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
            NavEntryDecorator(
                onPop = { key ->
                    predictiveBackHandler.onPagePop(
                        contentPageKey = key,
                        animationScope = navigationScope,
                    )
                }
            ) { entry ->
                with(predictiveBackHandler) {
                    Box(
                        modifier = Modifier.predictiveBackAnimationDecorator(
                            transitionState = navigationEventState?.transitionState,
                            contentPageKey = entry.contentKey,
                            currentPageKey = safeBackStack.lastOrNull(),
                        )
                    ) {
                        entry.Content()
                    }
                }
            }
        ),
        entryProvider = entryProvider
    )
    val sceneState = rememberSceneState(
        entries = entries,
        sceneStrategies = listOf(SinglePaneSceneStrategy()),
        sceneDecoratorStrategies = emptyList(),
        sharedTransitionScope = sharedTransitionScope,
        onBack = performBack
    )
    val scene = sceneState.currentScene
    val currentInfo = SceneInfo(scene)
    val previousSceneInfos = sceneState.previousScenes.map { SceneInfo(it) }
    navigationEventState = rememberNavigationEventState(
        currentInfo = currentInfo,
        backInfo = previousSceneInfos
    )

    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = scene.previousEntries.isNotEmpty(),
        onBackCompleted = performBack
    )

    NavDisplay(
        sceneState = sceneState,
        navigationEventState = navigationEventState,
        modifier = modifier,
        contentAlignment = Alignment.TopStart,
        sizeTransform = null,
        transitionSpec = {
            with(predictiveBackHandler) {
                onTransitionSpec()
            }
        },
        popTransitionSpec = {
            with(predictiveBackHandler) {
                onPopTransitionSpec()
            }
        },
        predictivePopTransitionSpec = { swipeEdge ->
            with(predictiveBackHandler) {
                onPredictivePopTransitionSpec(swipeEdge = swipeEdge)
            }
        },
    )
}

@Composable
private fun ProvideNavigation3ViewModelApplicationExtras(
    application: Application,
    content: @Composable () -> Unit
) {
    val navEntryOwner = LocalViewModelStoreOwner.current
    if (navEntryOwner == null) {
        content()
        return
    }

    val patchedOwner = remember(navEntryOwner, application) {
        buildNavigation3ViewModelStoreOwner(navEntryOwner, application)
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides patchedOwner) {
        content()
    }
}

private fun buildNavigation3ViewModelStoreOwner(
    navEntryOwner: ViewModelStoreOwner,
    application: Application
): ViewModelStoreOwner {
    val defaultFactoryOwner = navEntryOwner as? HasDefaultViewModelProviderFactory
    val defaultCreationExtras = defaultFactoryOwner?.defaultViewModelCreationExtras
        ?: CreationExtras.Empty
    val patchedCreationExtras = MutableCreationExtras(defaultCreationExtras).apply {
        set(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY, application)
    }

    return object : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
        override val viewModelStore = navEntryOwner.viewModelStore
        override val defaultViewModelProviderFactory =
            defaultFactoryOwner?.defaultViewModelProviderFactory
                ?: ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        override val defaultViewModelCreationExtras: CreationExtras = patchedCreationExtras
    }
}