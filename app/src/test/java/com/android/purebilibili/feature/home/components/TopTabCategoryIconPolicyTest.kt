package com.android.purebilibili.feature.home.components

import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.purebilibili.core.ui.AppSemanticIconFamily
import io.github.alexzhirkevich.cupertino.icons.filled.Cpu as FilledCupertinoCpu
import io.github.alexzhirkevich.cupertino.icons.filled.Gamecontroller as FilledCupertinoGamecontroller
import io.github.alexzhirkevich.cupertino.icons.outlined.Cpu as OutlinedCupertinoCpu
import io.github.alexzhirkevich.cupertino.icons.outlined.Gamecontroller as OutlinedCupertinoGamecontroller
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import androidx.compose.material.icons.Icons
import kotlin.test.Test
import kotlin.test.assertEquals

class TopTabCategoryIconPolicyTest {

    @Test
    fun topTabCategoryIconPolicy_usesSemanticIosIcons() {
        assertSameVectorAsset(
            CupertinoIcons.Outlined.OutlinedCupertinoGamecontroller,
            resolveTopTabCategoryIcon("游戏", AppSemanticIconFamily.CUPERTINO, selected = false)
        )
        assertSameVectorAsset(
            CupertinoIcons.Filled.FilledCupertinoGamecontroller,
            resolveTopTabCategoryIcon("游戏", AppSemanticIconFamily.CUPERTINO, selected = true)
        )
        assertSameVectorAsset(
            CupertinoIcons.Outlined.OutlinedCupertinoCpu,
            resolveTopTabCategoryIcon("科技", AppSemanticIconFamily.CUPERTINO, selected = false)
        )
        assertSameVectorAsset(
            CupertinoIcons.Filled.FilledCupertinoCpu,
            resolveTopTabCategoryIcon("科技", AppSemanticIconFamily.CUPERTINO, selected = true)
        )
    }

    @Test
    fun topTabCategoryIconPolicy_usesSemanticMd3Icons() {
        assertSameVectorAsset(
            Icons.Outlined.SportsEsports,
            resolveTopTabCategoryIcon("游戏", AppSemanticIconFamily.MATERIAL, selected = false)
        )
        assertSameVectorAsset(
            Icons.Filled.SportsEsports,
            resolveTopTabCategoryIcon("游戏", AppSemanticIconFamily.MATERIAL, selected = true)
        )
        assertSameVectorAsset(
            Icons.Outlined.SmartToy,
            resolveTopTabCategoryIcon("科技", AppSemanticIconFamily.MATERIAL, selected = false)
        )
        assertSameVectorAsset(
            Icons.Filled.SmartToy,
            resolveTopTabCategoryIcon("科技", AppSemanticIconFamily.MATERIAL, selected = true)
        )
    }

    @Test
    fun topTabCategoryIconPolicy_matchesFloatingBottomBarFamily() {
        assertEquals(
            AppSemanticIconFamily.CUPERTINO,
            resolveTopTabIconFamily(
                chromeIconFamily = AppSemanticIconFamily.MATERIAL,
                useBottomBarMatchedChrome = true
            )
        )
        assertEquals(
            AppSemanticIconFamily.MATERIAL,
            resolveTopTabIconFamily(
                chromeIconFamily = AppSemanticIconFamily.MATERIAL,
                useBottomBarMatchedChrome = false
            )
        )
    }

    private fun assertSameVectorAsset(expected: ImageVector, actual: ImageVector) {
        assertEquals(expected.name, actual.name)
        assertEquals(expected.defaultWidth, actual.defaultWidth)
        assertEquals(expected.defaultHeight, actual.defaultHeight)
        assertEquals(expected.viewportWidth, actual.viewportWidth)
        assertEquals(expected.viewportHeight, actual.viewportHeight)
    }
}
