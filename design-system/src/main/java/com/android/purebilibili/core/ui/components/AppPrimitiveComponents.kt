package com.android.purebilibili.core.ui.components

import com.android.purebilibili.core.theme.LocalAppUiStyle
import com.android.purebilibili.core.ui.resolveFilledButtonContainerColor
import com.android.purebilibili.core.ui.resolveFilledButtonContentColor
import com.android.purebilibili.core.ui.AppChromeSizeTokens
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.ChipElevation
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.android.purebilibili.core.ui.LocalAppThemeConfig
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.window.WindowListPopup
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SelectableChipElevation
import androidx.compose.material3.InputChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults as MiuixTextFieldDefaults
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonColors as MiuixButtonColors
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val APP_TAB_INDICATOR_DURATION_MILLIS = 300
private val flutterEase = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
private val PiliPlusIndicatorDecelerate = Easing { fraction ->
    kotlin.math.sin(flutterEase.transform(fraction) * Math.PI.toFloat() / 2f)
}
private val PiliPlusIndicatorAccelerate = Easing { fraction ->
    1f - kotlin.math.cos(flutterEase.transform(fraction) * Math.PI.toFloat() / 2f)
}

internal data class ElasticTabIndicatorBounds(
    val leftDp: Float,
    val widthDp: Float,
)

internal fun resolveElasticTabIndicatorBounds(
    position: Float,
    tabLeftsDp: List<Float>,
    tabWidthsDp: List<Float>,
    contentWidthsDp: List<Float>,
    matchContentSize: Boolean,
): ElasticTabIndicatorBounds {
    if (
        tabLeftsDp.isEmpty() ||
        tabLeftsDp.size != tabWidthsDp.size ||
        tabLeftsDp.size != contentWidthsDp.size
    ) {
        return ElasticTabIndicatorBounds(leftDp = 0f, widthDp = 0f)
    }
    val lastIndex = tabLeftsDp.lastIndex
    val safePosition = position.coerceIn(0f, lastIndex.toFloat())
    val startIndex = kotlin.math.floor(safePosition).toInt()
    val endIndex = (startIndex + 1).coerceAtMost(lastIndex)
    val fraction = safePosition - startIndex

    fun edges(index: Int): Pair<Float, Float> {
        val width = if (matchContentSize) contentWidthsDp[index] else tabWidthsDp[index]
        val left = tabLeftsDp[index] + (tabWidthsDp[index] - width) / 2f
        return left to (left + width)
    }

    val (startLeft, startRight) = edges(startIndex)
    val (endLeft, endRight) = edges(endIndex)
    val trailingEdgeProgress = 1f - kotlin.math.cos(fraction * Math.PI.toFloat() / 2f)
    val leadingEdgeProgress = kotlin.math.sin(fraction * Math.PI.toFloat() / 2f)
    val left = startLeft + (endLeft - startLeft) * trailingEdgeProgress
    val right = startRight + (endRight - startRight) * leadingEdgeProgress
    return ElasticTabIndicatorBounds(
        leftDp = left,
        widthDp = (right - left).coerceAtLeast(0f),
    )
}

@Composable
private fun AppElasticTabIndicator(
    selectedTabIndex: Int,
    tabPositions: List<TabPosition>,
    matchContentSize: Boolean,
    primary: Boolean,
    indicatorPositionProvider: (() -> Float)? = null,
) {
    if (tabPositions.isEmpty()) return
    val followPosition = indicatorPositionProvider?.invoke()
    val safeIndex = selectedTabIndex.coerceIn(tabPositions.indices)
    val previousIndex = remember { mutableIntStateOf(selectedTabIndex) }
    val movingRight = remember(safeIndex) { safeIndex >= previousIndex.intValue }
    val target = tabPositions[safeIndex]
    val targetWidth = if (matchContentSize) target.contentWidth else target.width
    val targetLeft = target.left + (target.width - targetWidth) / 2f
    val targetRight = targetLeft + targetWidth
    val animatedLeft by animateDpAsState(
        targetValue = targetLeft,
        animationSpec = tween(
            durationMillis = APP_TAB_INDICATOR_DURATION_MILLIS,
            easing = if (movingRight) PiliPlusIndicatorAccelerate else PiliPlusIndicatorDecelerate,
        ),
        label = "appTabIndicatorLeft",
    )
    val animatedRight by animateDpAsState(
        targetValue = targetRight,
        animationSpec = tween(
            durationMillis = APP_TAB_INDICATOR_DURATION_MILLIS,
            easing = if (movingRight) PiliPlusIndicatorDecelerate else PiliPlusIndicatorAccelerate,
        ),
        label = "appTabIndicatorRight",
    )
    SideEffect {
        previousIndex.intValue = safeIndex
    }
    val left: Dp
    val right: Dp
    if (followPosition != null) {
        val bounds = resolveElasticTabIndicatorBounds(
            position = followPosition,
            tabLeftsDp = tabPositions.map { it.left.value },
            tabWidthsDp = tabPositions.map { it.width.value },
            contentWidthsDp = tabPositions.map { it.contentWidth.value },
            matchContentSize = matchContentSize,
        )
        left = bounds.leftDp.dp
        right = (bounds.leftDp + bounds.widthDp).dp
    } else {
        left = animatedLeft
        right = animatedRight
    }
    val indicatorModifier = Modifier
        // Legacy TabRow measures the indicator slot to the full row. Recreate Material's
        // indicator host contract so the elastic line stays pinned above the bottom divider.
        .fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .offset(x = left)
        .width((right - left).coerceAtLeast(0.dp))
    if (primary) {
        TabRowDefaults.PrimaryIndicator(
            modifier = indicatorModifier,
            width = Dp.Unspecified,
        )
    } else {
        TabRowDefaults.SecondaryIndicator(modifier = indicatorModifier)
    }
}

@Composable
fun AppSnackbar(
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) = Snackbar(
    modifier = modifier,
    action = action,
    content = content,
)

@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) = SnackbarHost(
    hostState = hostState,
    modifier = modifier,
)

@Suppress("DEPRECATION")
@Composable
fun AppScrollableTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = TabRowDefaults.primaryContainerColor,
    contentColor: Color = TabRowDefaults.primaryContentColor,
    edgePadding: Dp = TabRowDefaults.ScrollableTabRowEdgeStartPadding,
    indicatorPositionProvider: (() -> Float)? = null,
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit = @Composable { tabPositions ->
        AppElasticTabIndicator(
            selectedTabIndex = selectedTabIndex,
            tabPositions = tabPositions,
            matchContentSize = true,
            primary = false,
            indicatorPositionProvider = indicatorPositionProvider,
        )
    },
    divider: @Composable () -> Unit = @Composable { HorizontalDivider() },
    tabs: @Composable () -> Unit,
) = ScrollableTabRow(
    selectedTabIndex = selectedTabIndex,
    modifier = modifier,
    containerColor = containerColor,
    contentColor = contentColor,
    edgePadding = edgePadding,
    indicator = indicator,
    divider = divider,
    tabs = tabs,
)

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape,
    containerColor: Color,
    contentColor: Color,
    disabledContainerColor: Color = containerColor.copy(alpha = 0.38f),
    disabledContentColor: Color = contentColor.copy(alpha = 0.38f),
    border: BorderStroke? = null,
    defaultElevation: Dp = 0.dp,
    pressedElevation: Dp = defaultElevation,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val interactionModifier = modifier.appDesktopInteractionVisuals(resolvedInteractionSource, enabled)
    when (LocalAppUiStyle.current) {
        com.android.purebilibili.core.theme.AppUiStyle.MATERIAL3 -> Button(
            onClick = onClick,
            modifier = interactionModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = disabledContainerColor,
                disabledContentColor = disabledContentColor,
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = defaultElevation,
                pressedElevation = pressedElevation,
            ),
            border = border,
            contentPadding = contentPadding,
            interactionSource = resolvedInteractionSource,
            content = content,
        )
        com.android.purebilibili.core.theme.AppUiStyle.MIUIX -> MiuixButton(
            onClick = onClick,
            modifier = interactionModifier,
            enabled = enabled,
            colors = MiuixButtonColors(
                color = containerColor,
                disabledColor = disabledContainerColor,
                contentColor = contentColor,
                disabledContentColor = disabledContentColor,
            ),
            insideMargin = contentPadding,
            minHeight = AppChromeSizeTokens.MiuixNativeCompactControlHeightDp.dp,
            minWidth = AppChromeSizeTokens.MiuixNativeCompactControlMinWidthDp.dp,
            cornerRadius = AppChromeSizeTokens.MiuixNativeCompactCornerRadiusDp.dp,
            interactionSource = resolvedInteractionSource,
            content = content,
        )
    }
}

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors? = null,
    elevation: androidx.compose.material3.ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val interactionModifier = modifier.appDesktopInteractionVisuals(resolvedInteractionSource, enabled)
    when (LocalAppUiStyle.current) {
        com.android.purebilibili.core.theme.AppUiStyle.MATERIAL3 -> Button(
            onClick = onClick,
            modifier = interactionModifier,
            enabled = enabled,
            shape = shape,
            colors = colors ?: ButtonDefaults.buttonColors(
                containerColor = resolveFilledButtonContainerColor(MaterialTheme.colorScheme),
                contentColor = resolveFilledButtonContentColor(MaterialTheme.colorScheme),
            ),
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            interactionSource = resolvedInteractionSource,
            content = content,
        )
        com.android.purebilibili.core.theme.AppUiStyle.MIUIX -> MiuixButton(
            onClick = onClick,
            modifier = interactionModifier,
            enabled = enabled,
            colors = colors?.let {
                MiuixButtonColors(
                    color = it.containerColor,
                    disabledColor = it.disabledContainerColor,
                    contentColor = it.contentColor,
                    disabledContentColor = it.disabledContentColor,
                )
            } ?: MiuixButtonDefaults.buttonColorsPrimary(),
            insideMargin = contentPadding,
            minHeight = AppChromeSizeTokens.MiuixNativeCompactControlHeightDp.dp,
            minWidth = AppChromeSizeTokens.MiuixNativeCompactControlMinWidthDp.dp,
            cornerRadius = AppChromeSizeTokens.MiuixNativeCompactCornerRadiusDp.dp,
            interactionSource = resolvedInteractionSource,
            content = content,
        )
    }
}

@Composable
fun AppTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.textShape,
    colors: androidx.compose.material3.ButtonColors = ButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val interactionModifier = modifier.appDesktopInteractionVisuals(resolvedInteractionSource, enabled)
    when (LocalAppUiStyle.current) {
        com.android.purebilibili.core.theme.AppUiStyle.MATERIAL3 -> TextButton(
            onClick = onClick,
            modifier = interactionModifier,
            enabled = enabled,
            shape = shape,
            colors = colors,
            contentPadding = contentPadding,
            interactionSource = resolvedInteractionSource,
            content = content,
        )
        com.android.purebilibili.core.theme.AppUiStyle.MIUIX -> MiuixButton(
            onClick = onClick,
            modifier = interactionModifier,
            enabled = enabled,
            colors = MiuixButtonColors(
                color = colors.containerColor,
                disabledColor = colors.disabledContainerColor,
                contentColor = colors.contentColor,
                disabledContentColor = colors.disabledContentColor,
            ),
            insideMargin = contentPadding,
            minHeight = AppChromeSizeTokens.MiuixNativeCompactControlHeightDp.dp,
            minWidth = AppChromeSizeTokens.MiuixNativeCompactControlMinWidthDp.dp,
            cornerRadius = AppChromeSizeTokens.MiuixNativeCompactCornerRadiusDp.dp,
            interactionSource = resolvedInteractionSource,
            content = content,
        )
    }
}

@Composable
fun AppOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = androidx.compose.material3.LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    labelText: String? = null,
    placeholderText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    if (
        shouldUseMiuixOutlinedTextField(
            uiStyle = LocalAppUiStyle.current,
            hasPrefix = prefix != null,
            hasSuffix = suffix != null,
        )
    ) {
        val resolvedLabel = labelText ?: placeholderText.orEmpty()
        MiuixTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            label = resolvedLabel,
            useLabelAsPlaceholder = labelText == null && !placeholderText.isNullOrEmpty(),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            colors = MiuixTextFieldDefaults.textFieldColors(
                borderColor = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MiuixTheme.colorScheme.primary
                },
            ),
        )
        supportingText?.invoke()
        return
    }
    val resolvedLabel = label ?: labelText?.let { text -> { Text(text) } }
    val resolvedPlaceholder = placeholder ?: placeholderText?.let { text -> { Text(text) } }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = resolvedLabel,
        placeholder = resolvedPlaceholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
    )
}

@Composable
fun AppDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero,
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (LocalAppThemeConfig.current.nativeMiuixPopupsEnabled) {
        WindowListPopup(
            show = expanded,
            popupModifier = modifier,
            onDismissRequest = onDismissRequest,
        ) {
            ListPopupColumn {
                Column(content = content)
            }
        }
    } else {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            offset = offset,
            scrollState = scrollState,
            properties = properties,
            content = content,
        )
    }
}

@Composable
fun AppDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource? = null,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    DropdownMenuItem(
        text = text,
        onClick = onClick,
        modifier = modifier.appDesktopInteractionVisuals(resolvedInteractionSource, enabled),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = resolvedInteractionSource,
    )
}

@Composable
fun AppModalNavigationDrawer(
    drawerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    gesturesEnabled: Boolean = true,
    scrimColor: Color,
    content: @Composable () -> Unit,
) = ModalNavigationDrawer(
    drawerContent = drawerContent,
    modifier = modifier,
    drawerState = drawerState,
    gesturesEnabled = gesturesEnabled,
    scrimColor = scrimColor,
    content = content,
)

@Composable
fun AppNavigationDrawerItem(
    label: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    colors: NavigationDrawerItemColors = NavigationDrawerItemDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    NavigationDrawerItem(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier.appDesktopInteractionVisuals(resolvedInteractionSource),
        icon = icon,
        badge = badge,
        colors = colors,
        interactionSource = resolvedInteractionSource,
    )
}

@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: androidx.compose.material3.ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.appDesktopInteractionVisuals(resolvedInteractionSource, enabled),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = resolvedInteractionSource,
        content = content,
    )
}

@Composable
fun AppAssistChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = AssistChipDefaults.shape,
    colors: ChipColors = AssistChipDefaults.assistChipColors(),
    elevation: ChipElevation? = AssistChipDefaults.assistChipElevation(),
    border: BorderStroke? = AssistChipDefaults.assistChipBorder(enabled),
    horizontalArrangement: Arrangement.Horizontal = AssistChipDefaults.horizontalArrangement(),
    contentPadding: PaddingValues = AssistChipDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
) = AssistChip(
    onClick = onClick,
    label = label,
    modifier = modifier,
    enabled = enabled,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    shape = shape,
    colors = colors,
    elevation = elevation,
    border = border,
    horizontalArrangement = horizontalArrangement,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
)

@Composable
fun AppFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = FilterChipDefaults.shape,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(),
    elevation: SelectableChipElevation? = FilterChipDefaults.filterChipElevation(),
    border: BorderStroke? = FilterChipDefaults.filterChipBorder(enabled, selected),
    horizontalArrangement: Arrangement.Horizontal = FilterChipDefaults.horizontalArrangement(),
    contentPadding: PaddingValues = FilterChipDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
) = FilterChip(
    selected = selected,
    onClick = onClick,
    label = label,
    modifier = modifier,
    enabled = enabled,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    shape = shape,
    colors = colors,
    elevation = elevation,
    border = border,
    horizontalArrangement = horizontalArrangement,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
)

@Composable
fun AppInputChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) = InputChip(
    selected = selected,
    onClick = onClick,
    label = label,
    modifier = modifier,
    enabled = enabled,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
)

@Composable
fun AppFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) = FloatingActionButton(
    onClick = onClick,
    modifier = modifier,
    shape = shape,
    containerColor = containerColor,
    contentColor = contentColor,
    elevation = elevation,
    interactionSource = interactionSource,
    content = content,
)

@Composable
fun AppSmallFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.smallShape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) = SmallFloatingActionButton(
    onClick = onClick,
    modifier = modifier,
    shape = shape,
    containerColor = containerColor,
    contentColor = contentColor,
    elevation = elevation,
    interactionSource = interactionSource,
    content = content,
)

@Composable
fun AppTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    selectedContentColor: Color = LocalContentColor.current,
    unselectedContentColor: Color = selectedContentColor,
    interactionSource: MutableInteractionSource? = null,
) = Tab(
    selected = selected,
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    text = text,
    icon = icon,
    selectedContentColor = selectedContentColor,
    unselectedContentColor = unselectedContentColor,
    interactionSource = interactionSource,
)

@Composable
fun AppTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedContentColor: Color = LocalContentColor.current,
    unselectedContentColor: Color = selectedContentColor,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable ColumnScope.() -> Unit,
) = Tab(
    selected = selected,
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    selectedContentColor = selectedContentColor,
    unselectedContentColor = unselectedContentColor,
    interactionSource = interactionSource,
    content = content,
)

@Composable
fun AppPrimaryTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = TabRowDefaults.primaryContainerColor,
    contentColor: Color = TabRowDefaults.primaryContentColor,
    indicatorPositionProvider: (() -> Float)? = null,
    tabs: @Composable () -> Unit,
) {
    @Suppress("DEPRECATION")
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        indicator = { tabPositions ->
            AppElasticTabIndicator(
                selectedTabIndex = selectedTabIndex,
                tabPositions = tabPositions,
                matchContentSize = true,
                primary = true,
                indicatorPositionProvider = indicatorPositionProvider,
            )
        },
        tabs = tabs,
    )
}

@Composable
fun AppPrimaryScrollableTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    containerColor: Color = TabRowDefaults.primaryContainerColor,
    contentColor: Color = TabRowDefaults.primaryContentColor,
    edgePadding: Dp = TabRowDefaults.ScrollableTabRowEdgeStartPadding,
    minTabWidth: Dp = TabRowDefaults.ScrollableTabRowMinTabWidth,
    indicatorPositionProvider: (() -> Float)? = null,
    tabs: @Composable () -> Unit,
) {
    @Suppress("DEPRECATION")
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        edgePadding = edgePadding,
        indicator = { tabPositions ->
            AppElasticTabIndicator(
                selectedTabIndex = selectedTabIndex,
                tabPositions = tabPositions,
                matchContentSize = true,
                primary = true,
                indicatorPositionProvider = indicatorPositionProvider,
            )
        },
        tabs = tabs,
    )
}

@Composable
fun AppSuggestionChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    shape: Shape = SuggestionChipDefaults.shape,
    colors: ChipColors = SuggestionChipDefaults.suggestionChipColors(),
    elevation: ChipElevation? = SuggestionChipDefaults.suggestionChipElevation(),
    border: BorderStroke? = SuggestionChipDefaults.suggestionChipBorder(enabled),
    horizontalArrangement: Arrangement.Horizontal = SuggestionChipDefaults.horizontalArrangement(),
    contentPadding: PaddingValues = SuggestionChipDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
) = SuggestionChip(
    onClick = onClick,
    label = label,
    modifier = modifier,
    enabled = enabled,
    icon = icon,
    shape = shape,
    colors = colors,
    elevation = elevation,
    border = border,
    horizontalArrangement = horizontalArrangement,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
)
