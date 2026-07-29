package com.android.purebilibili.feature.search

import com.android.purebilibili.core.ui.AppTopTabPresentation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchTopBarLayoutPolicyTest {

    @Test
    fun topBarLayout_removesInlineHotToggleAndKeepsPlaceholderSingleLine() {
        val spec = resolveSearchTopBarLayoutSpec()

        assertFalse(spec.showInlineHotToggle)
        assertEquals(1, spec.placeholderMaxLines)
    }

    @Test
    fun topBarRowMinHeight_accommodatesInputHeightAndVerticalPadding() {
        assertEquals(72, resolveSearchTopBarRowMinHeightDp(inputHeightDp = 56))
        assertEquals(64, resolveSearchTopBarRowMinHeightDp(inputHeightDp = 44))
    }

    @Test
    fun material3SearchInput_omitsLeadingIconToPreservePlaceholderWidth() {
        assertTrue(
            shouldOmitSearchInputLeadingIcon(
                tabPresentation = AppTopTabPresentation.MATERIAL_UNDERLINE,
            )
        )
        assertFalse(
            shouldOmitSearchInputLeadingIcon(
                tabPresentation = AppTopTabPresentation.TONAL_CAPSULE,
            )
        )
        assertFalse(
            shouldOmitSearchInputLeadingIcon(
                tabPresentation = AppTopTabPresentation.MOVING_CAPSULE,
            )
        )
    }
}
