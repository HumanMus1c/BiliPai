package com.android.purebilibili.navigation3

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class BiliPaiNavContentTransformPolicyStructureTest {

    @Test
    fun reducedMotionUsesShortCrossfadeWithoutSpatialTranslation() {
        val source = contentTransformPolicySource()
        val reducedBranch = source
            .substringAfter("BiliPaiNavRouteTransition.REDUCED_MOTION_FADE ->")
            .substringBefore("BiliPaiNavRouteTransition.CARD_DISABLED_VIDEO_FORWARD_FROM_LEFT")

        assertTrue(source.contains("NAV3_REDUCED_MOTION_FADE_MILLIS = 140"))
        assertTrue(reducedBranch.contains("fadeIn("))
        assertTrue(reducedBranch.contains("fadeOut("))
        assertTrue(reducedBranch.contains("slideInHorizontally(").not())
        assertTrue(reducedBranch.contains("slideOutHorizontally(").not())
    }

    @Test
    fun disabledVideoDirectionalReturnMovesTargetPageFromOppositeSide() {
        val source = contentTransformPolicySource()
        val returnFunctionStart = source.indexOf("private fun disabledVideoDirectionReturnTransform")
        val returnFunctionEnd = source.length
        val returnFunction = source.substring(returnFunctionStart, returnFunctionEnd)

        assertTrue(returnFunction.contains("slideInHorizontally("))
        assertTrue(returnFunction.contains("initialOffsetX = { width -> (-directionSign * width * 0.18f).toInt() }"))
    }

    @Test
    fun disabledVideoDirectionalReturnMovesBothPagesHorizontally() {
        val source = contentTransformPolicySource()
        val returnFunctionStart = source.indexOf("private fun disabledVideoDirectionReturnTransform")
        val returnFunctionEnd = source.length
        val returnFunction = source.substring(returnFunctionStart, returnFunctionEnd)

        assertTrue(returnFunction.contains("slideOutHorizontally("))
        assertTrue(returnFunction.contains("slideInHorizontally("))
    }

    @Test
    fun disabledVideoDirectionalReturnSlidesTowardCardColumn() {
        val source = contentTransformPolicySource()
        val returnFunctionStart = source.indexOf("private fun disabledVideoDirectionReturnTransform")
        val returnFunctionEnd = source.length
        val returnFunction = source.substring(returnFunctionStart, returnFunctionEnd)

        assertTrue(returnFunction.contains("targetOffsetX = { width -> (directionSign * width * 0.9f).toInt() }"))
    }

    @Test
    fun disabledVideoDirectionalReturnUsesResponsiveMotionWindow() {
        val source = contentTransformPolicySource()

        assertTrue(source.contains("NAV3_DISABLED_VIDEO_RETURN_MILLIS = 260"))
    }

    @Test
    fun disabledVideoDirectionalReturnFadesAlongsideSlide() {
        val source = contentTransformPolicySource()
        val returnFunctionStart = source.indexOf("private fun disabledVideoDirectionReturnTransform")
        val returnFunctionEnd = source.length
        val returnFunction = source.substring(returnFunctionStart, returnFunctionEnd)

        assertTrue(returnFunction.contains("fadeIn("))
        assertTrue(returnFunction.contains("fadeOut("))
    }

    @Test
    fun spaceForwardUsesLightSlideAndFade() {
        val source = contentTransformPolicySource()

        assertTrue(source.contains("BiliPaiNavRouteTransition.SPACE_FORWARD"))
        assertTrue(source.contains("private fun spaceForwardTransform()"))
        assertTrue(source.contains("initialOffsetX = { width -> width / 8 }"))
        assertTrue(source.contains("fadeIn(animationSpec = tween(NAV3_SPACE_FORWARD_MILLIS))"))
    }

    @Test
    fun lightSiblingForwardUsesSmallSlideAndFade() {
        val source = contentTransformPolicySource()
        val functionStart = source.indexOf("private fun lightSiblingForwardTransform")
        val functionEnd = source.indexOf("private fun lightSiblingPopTransform")
        val function = source.substring(functionStart, functionEnd)

        assertTrue(source.contains("BiliPaiNavRouteTransition.LIGHT_SIBLING_FORWARD"))
        assertTrue(function.contains("slideInHorizontally("))
        assertTrue(function.contains("initialOffsetX = { width -> width / 8 }"))
        assertTrue(function.contains("fadeIn(animationSpec = tween(NAV3_LIGHT_SIBLING_MILLIS"))
        assertTrue(function.contains("fadeOut(animationSpec = tween(NAV3_FALLBACK_FADE_MILLIS))"))
    }

    @Test
    fun lightSiblingPopMovesOnlyOutgoingPageSlightly() {
        val source = contentTransformPolicySource()
        val functionStart = source.indexOf("private fun lightSiblingPopTransform")
        val functionEnd = source.indexOf("private fun disabledVideoDirectionReturnTransform")
        val function = source.substring(functionStart, functionEnd)

        assertTrue(source.contains("BiliPaiNavRouteTransition.LIGHT_SIBLING_POP"))
        assertTrue(function.contains("EnterTransition.None togetherWith"))
        assertTrue(function.contains("slideOutHorizontally("))
        assertTrue(function.contains("targetOffsetX = { width -> width / 8 }"))
        assertTrue(function.contains("fadeOut(animationSpec = tween(NAV3_LIGHT_SIBLING_MILLIS"))
    }

    @Test
    fun settingsIosPushForwardUsesParallaxSlide() {
        val source = contentTransformPolicySource()

        assertTrue(source.contains("BiliPaiNavRouteTransition.SETTINGS_IOS_PUSH_FORWARD"))
        assertTrue(source.contains("private fun settingsIosPushForwardTransform()"))
        assertTrue(source.contains("resolveSettingsIosPushForwardContentTransform("))
    }

    @Test
    fun settingsIosPushPopUsesParallaxSlide() {
        val source = contentTransformPolicySource()

        assertTrue(source.contains("BiliPaiNavRouteTransition.SETTINGS_IOS_PUSH_POP"))
        assertTrue(source.contains("private fun settingsIosPushPopTransform()"))
        assertTrue(source.contains("resolveSettingsIosPushPopContentTransform("))
    }

    @Test
    fun bottomBarSiblingForwardUsesFullWidthHorizontalSlide() {
        val source = contentTransformPolicySource()

        assertTrue(source.contains("BiliPaiNavRouteTransition.BOTTOM_BAR_SIBLING_FORWARD"))
        assertTrue(source.contains("private fun bottomBarSiblingForwardTransform()"))
        assertTrue(source.contains("resolveBottomBarLikeHorizontalContentTransform("))
    }

    @Test
    fun bottomBarSiblingPopUsesFullWidthHorizontalSlide() {
        val source = contentTransformPolicySource()

        assertTrue(source.contains("BiliPaiNavRouteTransition.BOTTOM_BAR_SIBLING_POP"))
        assertTrue(source.contains("private fun bottomBarSiblingPopTransform()"))
    }

    @Test
    fun horizontalPageTransitionsUseCriticallyDampedSpring() {
        val tokenSource = designSystemSourceFile("core/ui/motion/NavigationSlideSpring.kt")
        val bottomBarSource = designSystemSourceFile(
            "core/ui/motion/BottomBarLikeContentTransformPolicy.kt"
        )
        val settingsSource = designSystemSourceFile(
            "core/ui/motion/SettingsIosPushContentTransformPolicy.kt"
        )
        val springToken = tokenSource
            .substringAfter("fun navigationSlideSpring(durationMillis: Int): SpringSpec<IntOffset>")
            .substringBefore("/**")

        assertTrue(tokenSource.contains("fun navigationSlideSpring(durationMillis: Int): SpringSpec<IntOffset>"))
        assertTrue(springToken.contains("dampingRatio = 1f"))
        assertTrue(springToken.contains("resolveNavigationSlideSpringStiffness(durationMillis)"))
        assertTrue(springToken.contains("visibilityThreshold = IntOffset(1, 1)"))
        assertTrue(bottomBarSource.contains("val spec = navigationSlideSpring(durationMillis)"))
        assertTrue(settingsSource.contains("resolveSettingsIosPushForwardContentTransform"))
        assertTrue(settingsSource.contains("val spec = navigationSlideSpring(durationMillis)"))
        assertTrue(contentTransformPolicySource().contains("navigationSlideSpring(NAV3_SPACE_FORWARD_MILLIS)"))
    }

    @Test
    fun settingsPredictivePopKeepsSeekableTween() {
        val source = designSystemSourceFile(
            "core/ui/motion/SettingsIosPushContentTransformPolicy.kt"
        )
        val predictiveFunction = source
            .substringAfter("fun resolveSettingsIosPredictivePopContentTransform")

        assertTrue(predictiveFunction.contains("tween("))
        assertTrue(predictiveFunction.contains("navigationSlideSpring(").not())
    }

    @Test
    fun settingsCommittedPopKeepsTweenForGestureContinuity() {
        val source = designSystemSourceFile(
            "core/ui/motion/SettingsIosPushContentTransformPolicy.kt"
        )
        val popFunction = source
            .substringAfter("fun resolveSettingsIosPushPopContentTransform")
            .substringBefore("fun resolveSettingsIosPredictivePopContentTransform")

        assertTrue(popFunction.contains("tween<IntOffset>("))
        assertTrue(popFunction.contains("navigationSlideSpring(").not())
    }

    private fun contentTransformPolicySource(): String {
        return sourceFile("navigation3/BiliPaiNavContentTransformPolicy.kt")
    }

    private fun sourceFile(relativePath: String): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/$relativePath"),
            File("src/main/java/com/android/purebilibili/$relativePath")
        ).first { it.exists() }.readText()
    }

    private fun designSystemSourceFile(relativePath: String): String {
        return listOf(
            File("design-system/src/main/java/com/android/purebilibili/$relativePath"),
            File("../design-system/src/main/java/com/android/purebilibili/$relativePath"),
        ).first { it.exists() }.readText()
    }
}
