package com.android.purebilibili.feature.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.purebilibili.R
import com.android.purebilibili.core.ui.rememberAppSettingsIcon
import com.android.purebilibili.core.ui.components.AppPreference
import com.android.purebilibili.core.ui.components.AppPreferenceDivider
import com.android.purebilibili.core.ui.components.AppPreferenceGroup
import com.android.purebilibili.core.ui.components.AppSearchEntry
import com.android.purebilibili.core.ui.components.AppSearchField
import com.android.purebilibili.core.ui.components.rememberAdaptiveListVisualCapabilities

@Composable
internal fun SettingsSearchBarSection(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val placeholder = stringResource(R.string.settings_search_placeholder)
    AppSearchField(
        query = query,
        onQueryChange = onQueryChange,
        placeholder = placeholder,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

@Composable
internal fun SettingsHomeSearchEntry(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visualSpec = resolveSettingsVisualSpec()
    val placeholder = stringResource(R.string.settings_search_placeholder)
    AppSearchEntry(
        onClick = onClick,
        placeholder = placeholder,
        modifier = modifier
            .padding(
                horizontal = visualSpec.screenHorizontalPadding,
                vertical = visualSpec.searchBarVerticalPadding,
            ),
    )
}

@Composable
internal fun SettingsSearchResultsSection(
    results: List<SettingsSearchResult>,
    onResultClick: (SettingsSearchResult) -> Unit,
) {
    val visualSpec = rememberAdaptiveListVisualCapabilities().componentSpec
    SettingsCategoryHeader(stringResource(R.string.settings_search_results_title))
    AppPreferenceGroup {
        if (results.isEmpty()) {
            AppPreference(
                icon = rememberAppSettingsIcon(),
                title = stringResource(R.string.settings_search_empty_title),
                subtitle = stringResource(R.string.settings_search_empty_subtitle),
                onClick = null,
                showChevron = false,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            results.forEachIndexed { index, result ->
                val visual = rememberSettingsEntryVisual(result.target)
                AppPreference(
                    icon = visual.icon,
                    iconPainter = visual.iconResId?.let { painterResource(id = it) },
                    title = result.title,
                    subtitle = result.subtitle,
                    value = result.section,
                    onClick = { onResultClick(result) },
                    iconTint = visual.iconTint,
                )
                if (index != results.lastIndex) {
                    AppPreferenceDivider(startIndent = visualSpec.dividerStartIndentDp.dp)
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
