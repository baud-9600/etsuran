package de.asdfghasdfgh.etsuran.view.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.asdfghasdfgh.etsuran.R

/** MMD's top bar: 64dp tall, the title in titleLarge, a 1dp rule underneath. */
val MmdTopAppBarHeight = 64.dp

/**
 * The top bar every Mudita screen wears: white surface, 24sp Lato title, actions flush
 * right, and a one-pixel rule separating it from the content.
 *
 * Material 2's [androidx.compose.material.TopAppBar] is 56dp and colours itself with
 * primarySurface (the accent, i.e. black in this app's default theme), which is the
 * opposite of MMD's white bar. This is drawn directly instead, so the rule and the
 * height are the same on every screen.
 */
@Composable
fun MmdTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = AppBarDefaults.topAppBarWindowInsets,
    showDivider: Boolean = true,
) {
    MmdTopAppBar(
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        showDivider = showDivider,
    ) {
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(start = if (navigationIcon == null) 12.dp else 0.dp),
            style = MaterialTheme.typography.h2,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colors.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Same bar with an arbitrary title slot, for search fields and other custom titles. */
@Composable
fun MmdTopAppBar(
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = AppBarDefaults.topAppBarWindowInsets,
    showDivider: Boolean = true,
    title: @Composable RowScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(windowInsets)
                .height(MmdTopAppBarHeight)
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigationIcon?.invoke()
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                title()
            }
            actions()
        }
        if (showDivider) {
            Divider(
                thickness = 1.dp,
                color = MaterialTheme.colors.onBackground,
            )
        }
    }
}

/**
 * MMD's switch: a 52x32dp capsule with a 2dp outline, filled when on. Never a tinted
 * slab and never an animated thumb — the thumb jumps, because on e-ink a slide is a
 * dozen full refreshes.
 */
@Composable
fun MmdSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val ink = MaterialTheme.colors.onBackground
    val paper = MaterialTheme.colors.background
    val trackColor = if (checked) ink else paper
    val thumbColor = if (checked) paper else ink
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier
            .size(width = 52.dp, height = 32.dp)
            .background(trackColor, shape)
            .border(2.dp, if (enabled) ink else ink.copy(alpha = 0.4f), shape)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null,
                onValueChange = { onCheckedChange?.invoke(it) },
            ),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(20.dp)
                .background(thumbColor, CircleShape),
        )
    }
}

/**
 * The main browser's MMD top bar: the current address in an outlined field
 * (tap it to edit) and the Settings gear on the right. Navigation stays in the
 * bottom toolbar; only the former burger menu lives up here.
 *
 * The address is shown rather than the page title: a title that opens an editor
 * is an invisible affordance, and a browser's top bar is where its address
 * belongs. The title remains on the tab row.
 */
@Composable
fun BrowserTopBar(
    address: String,
    onAddressClick: () -> Unit,
    onMenuClick: () -> Unit,
) {
    MmdTopAppBar(
        actions = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.menu),
                    tint = MaterialTheme.colors.onBackground,
                )
            }
        },
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .border(1.dp, MaterialTheme.colors.onBackground, RoundedCornerShape(8.dp))
                .clickable(onClick = onAddressClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Outlined.Public,
                contentDescription = null,
                tint = MaterialTheme.colors.onBackground,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = address.ifBlank { stringResource(R.string.app_name) },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
