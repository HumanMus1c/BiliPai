package com.android.purebilibili.navigation3

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
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
    fun disabledVideoDirectionalReturnEntersHomeFromLeftQuarter() {
        val source = contentTransformPolicySource()
        val returnFunctionStart = source.indexOf("private fun disabledVideoDirectionReturnTransform")
        val returnFunctionEnd = source.length
        val returnFunction = source.substring(returnFunctionStart, returnFunctionEnd)

        assertTrue(returnFunction.contains("slideInHorizontally("))
        assertTrue(
            returnFunction.contains(
                "initialOffsetX = { width -> -(width * NAV3_DISABLED_VIDEO_NATIVE_PARALLAX).toInt() }"
            )
        )
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
    fun disabledVideoDirectionalReturnExitsPlayerFullWidthToTheRight() {
        val source = contentTransformPolicySource()
        val returnFunctionStart = source.indexOf("private fun disabledVideoDirectionReturnTransform")
        val returnFunctionEnd = source.length
        val returnFunction = source.substring(returnFunctionStart, returnFunctionEnd)

        assertTrue(returnFunction.contains("targetOffsetX = { width -> width }"))
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
    fun disabledVideoDirectionalForwardEntersPlayerFullWidthFromTheRight() {
        val source = contentTransformPolicySource()
        val forwardFunctionStart = source.indexOf("private fun disabledVideoDirectionForwardTransform")
        val forwardFunctionEnd = source.indexOf("private fun disabledVideoDirectionReturnTransform")
        val forwardFunction = source.substring(forwardFunctionStart, forwardFunctionEnd)

        assertTrue(forwardFunction.contains("slideInHorizontally("))
        assertTrue(forwardFunction.contains("initialOffsetX = { width -> width }"))
        assertTrue(forwardFunction.contains("slideOutHorizontally("))
    }

    @Test
    fun disabledVideoDirectionalTransformsUseNativeTweenWithoutSpring() {
        val source = contentTransformPolicySource()
        val forwardFunctionStart = source.indexOf("private fun disabledVideoDirectionForwardTransform")
        val forwardFunctionEnd = source.indexOf("private fun spaceForwardTransform")
        val forwardFunction = source.substring(forwardFunctionStart, forwardFunctionEnd)
        val returnFunction = source.substring(
            source.indexOf("private fun disabledVideoDirectionReturnTransform")
        )

        assertTrue(forwardFunction.contains("FastOutSlowInEasing"))
        assertTrue(returnFunction.contains("FastOutSlowInEasing"))
        // 关闭过渡动画后的原生横滑禁止 spring：弹簧回弹与部分位移正是旧实现“复杂”的来源。
        assertTrue(forwardFunction.contains("navigationSlideSpring").not())
        assertTrue(returnFunction.contains("navigationSlideSpring").not())
    }

    @Test
    fun disabledVideoDirectionVariantsShareSingleNativeTransform() {
        val source = contentTransformPolicySource()
        // when 分支把 LEFT/RIGHT 两个历史分类名合并到同一个原生 transform（无 directionSign 参数）。
        // 用 \s* 匹配换行符，兼容 CRLF/LF 工作区。
        assertTrue(
            Regex(
                "CARD_DISABLED_VIDEO_FORWARD_FROM_LEFT,\\s*" +
                    "BiliPaiNavRouteTransition.CARD_DISABLED_VIDEO_FORWARD_FROM_RIGHT ->"
            ).containsMatchIn(source)
        )
        assertTrue(
            Regex(
                "CARD_DISABLED_VIDEO_RETURN_TO_LEFT,\\s*" +
                    "BiliPaiNavRouteTransition.CARD_DISABLED_VIDEO_RETURN_TO_RIGHT ->"
            ).containsMatchIn(source)
        )
        assertTrue(source.contains("disabledVideoDirectionForwardTransform(directionSign").not())
        assertTrue(source.contains("disabledVideoDirectionReturnTransform(directionSign").not())
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
    fun settingsIosPushForward_slidesOnlyTopPageFromRightWithoutSpring() {
        val appSource = contentTransformPolicySource()
        val settingsSource = designSystemSourceFile("core/ui/motion/SettingsIosPushContentTransformPolicy.kt")

        assertTrue(appSource.contains("BiliPaiNavRouteTransition.SETTINGS_IOS_PUSH_FORWARD"))
        assertTrue(appSource.contains("settingsIosPushForwardTransform()"))
        assertTrue(appSource.contains("resolveSettingsIosPushForwardContentTransform(durationMillis = SETTINGS_IOS_PUSH_DURATION_MS)"))

        val forward = settingsSource.substringAfter("fun resolveSettingsIosPushForwardContentTransform")
            .substringBefore("fun resolveSettingsIosPushPopContentTransform")
        assertTrue(forward.contains("slideInHorizontally("))
        assertTrue(forward.contains("initialOffsetX = { it }"))
        assertTrue(forward.contains("emphasizedEnterTween"))
        assertTrue(forward.contains("ExitTransition.None"))
        assertFalse(forward.contains("navigationSlideSpring"))
    }

    @Test
    fun settingsIosPushPop_slidesOnlyTopPageOutToRightWithoutSpring() {
        val appSource = contentTransformPolicySource()
        val settingsSource = designSystemSourceFile("core/ui/motion/SettingsIosPushContentTransformPolicy.kt")

        assertTrue(appSource.contains("BiliPaiNavRouteTransition.SETTINGS_IOS_PUSH_POP"))
        assertTrue(appSource.contains("settingsIosPushPopTransform()"))
        assertTrue(appSource.contains("resolveSettingsIosPushPopContentTransform(durationMillis = SETTINGS_IOS_PUSH_DURATION_MS)"))

        val pop = settingsSource.substringAfter("fun resolveSettingsIosPushPopContentTransform")
            .substringBefore("fun resolveSettingsIosPredictivePopContentTransform")
        assertTrue(pop.contains("slideOutHorizontally("))
        assertTrue(pop.contains("targetOffsetX = { it }"))
        assertTrue(pop.contains("emphasizedExitTween"))
        assertTrue(pop.contains("EnterTransition.None"))
        assertFalse(pop.contains("navigationSlideSpring"))
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
        val springToken = tokenSource
            .substringAfter("fun navigationSlideSpring(durationMillis: Int): SpringSpec<IntOffset>")
            .substringBefore("/**")

        assertTrue(tokenSource.contains("fun navigationSlideSpring(durationMillis: Int): SpringSpec<IntOffset>"))
        assertTrue(springToken.contains("dampingRatio = 1f"))
        assertTrue(springToken.contains("resolveNavigationSlideSpringStiffness(durationMillis)"))
        assertTrue(springToken.contains("visibilityThreshold = IntOffset(1, 1)"))
        assertTrue(bottomBarSource.contains("val spec = navigationSlideSpring(durationMillis)"))
        // 设置页 push/pop 明确不走 spring（见 settingsIosPushForward/Pop 结构断言），
        // 其余横向转场继续使用临界阻尼 spring。
        assertTrue(contentTransformPolicySource().contains("navigationSlideSpring(NAV3_SPACE_FORWARD_MILLIS)"))
    }

    @Test
    fun settingsPredictivePop_keepsLinearSeekWithTargetFullScreen() {
        val settingsSource = designSystemSourceFile("core/ui/motion/SettingsIosPushContentTransformPolicy.kt")
        val predictive = settingsSource.substringAfter("fun resolveSettingsIosPredictivePopContentTransform")

        assertTrue(predictive.contains("targetContentEnter = EnterTransition.None"))
        assertTrue(predictive.contains("initialContentExit = slideOutHorizontally("))
        assertTrue(predictive.contains("targetOffsetX = { it }"))
        assertTrue(predictive.contains("LinearEasing"))
        assertFalse(predictive.contains("navigationSlideSpring"))
    }

    @Test
    fun settingsTransformsGuardZeroDurationWithNoOpTransforms() {
        val settingsSource = designSystemSourceFile("core/ui/motion/SettingsIosPushContentTransformPolicy.kt")

        val forward = settingsSource.substringAfter("fun resolveSettingsIosPushForwardContentTransform")
            .substringBefore("fun resolveSettingsIosPushPopContentTransform")
        val pop = settingsSource.substringAfter("fun resolveSettingsIosPushPopContentTransform")
            .substringBefore("fun resolveSettingsIosPredictivePopContentTransform")
        val predictive = settingsSource.substringAfter("fun resolveSettingsIosPredictivePopContentTransform")

        assertTrue(forward.contains("if (durationMillis <= 0)"))
        assertTrue(forward.contains("EnterTransition.None togetherWith ExitTransition.None"))
        assertTrue(pop.contains("if (durationMillis <= 0)"))
        assertTrue(pop.contains("EnterTransition.None togetherWith ExitTransition.None"))
        assertTrue(predictive.contains("if (durationMillis <= 0)"))
        assertTrue(predictive.contains("EnterTransition.None togetherWith ExitTransition.None"))
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
