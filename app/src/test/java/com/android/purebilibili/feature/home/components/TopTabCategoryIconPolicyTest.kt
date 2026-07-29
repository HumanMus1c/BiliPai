package com.android.purebilibili.feature.home.components

import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.purebilibili.core.ui.AppSemanticIconFamily
import io.github.alexzhirkevich.cupertino.icons.outlined.Cpu
import io.github.alexzhirkevich.cupertino.icons.outlined.PlayCircle
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import androidx.compose.material.icons.Icons
import kotlin.test.Test
import kotlin.test.assertEquals

class TopTabCategoryIconPolicyTest {

    @Test
    fun topTabCategoryIconPolicy_usesSemanticIosIcons() {
        assertSameVectorAsset(CupertinoIcons.Outlined.PlayCircle, resolveTopTabCategoryIcon("游戏", AppSemanticIconFamily.CUPERTINO))
        assertSameVectorAsset(CupertinoIcons.Outlined.Cpu, resolveTopTabCategoryIcon("科技", AppSemanticIconFamily.CUPERTINO))
    }

    @Test
    fun topTabCategoryIconPolicy_usesSemanticMd3Icons() {
        assertSameVectorAsset(Icons.Outlined.SportsEsports, resolveTopTabCategoryIcon("游戏", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.Outlined.SmartToy, resolveTopTabCategoryIcon("科技", AppSemanticIconFamily.MATERIAL))
    }

    private fun assertSameVectorAsset(expected: ImageVector, actual: ImageVector) {
        assertEquals(expected.name, actual.name)
        assertEquals(expected.defaultWidth, actual.defaultWidth)
        assertEquals(expected.defaultHeight, actual.defaultHeight)
        assertEquals(expected.viewportWidth, actual.viewportWidth)
        assertEquals(expected.viewportHeight, actual.viewportHeight)
    }
}
