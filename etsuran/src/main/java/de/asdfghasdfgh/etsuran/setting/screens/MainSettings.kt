package de.asdfghasdfgh.etsuran.setting.screens

import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.activity.SettingRoute.About
import de.asdfghasdfgh.etsuran.activity.SettingRoute.Backup
import de.asdfghasdfgh.etsuran.activity.SettingRoute.Behavior
import de.asdfghasdfgh.etsuran.activity.SettingRoute.DataControl
import de.asdfghasdfgh.etsuran.activity.SettingRoute.Gesture
import de.asdfghasdfgh.etsuran.activity.SettingRoute.Misc
import de.asdfghasdfgh.etsuran.activity.SettingRoute.Search
import de.asdfghasdfgh.etsuran.activity.SettingRoute.StartControl
import de.asdfghasdfgh.etsuran.activity.SettingRoute.Toolbar
import de.asdfghasdfgh.etsuran.activity.SettingRoute.Ui
import de.asdfghasdfgh.etsuran.setting.DividerSettingItem
import de.asdfghasdfgh.etsuran.setting.LinkSettingItem
import de.asdfghasdfgh.etsuran.setting.NavigateSettingItem
import de.asdfghasdfgh.etsuran.setting.SettingItemInterface
import de.asdfghasdfgh.etsuran.setting.VersionSettingItem

fun buildMainSettingItems(): List<SettingItemInterface> = listOf(
    NavigateSettingItem(R.string.setting_title_ui, R.drawable.ic_phone, destination = Ui),
    NavigateSettingItem(
        R.string.setting_title_toolbar,
        R.drawable.ic_toolbar,
        destination = Toolbar
    ),
    NavigateSettingItem(
        R.string.setting_title_behavior,
        R.drawable.icon_ui,
        destination = Behavior
    ),
    NavigateSettingItem(
        R.string.setting_gestures,
        R.drawable.gesture_tap,
        destination = Gesture
    ),
    DividerSettingItem(),
    NavigateSettingItem(
        R.string.setting_title_data,
        R.drawable.icon_backup,
        destination = Backup
    ),
    NavigateSettingItem(
        R.string.setting_title_start_control,
        R.drawable.icon_earth,
        destination = StartControl
    ),
    NavigateSettingItem(
        R.string.setting_title_clear_control,
        R.drawable.ic_data,
        destination = DataControl
    ),
    NavigateSettingItem(
        R.string.setting_title_search,
        R.drawable.icon_search,
        destination = Search
    ),
    DividerSettingItem(),
    NavigateSettingItem(
        R.string.misc,
        R.drawable.icon_dots,
        destination = Misc
    ),
    LinkSettingItem.Manual,
    VersionSettingItem(
        R.string.menu_other_info,
        R.drawable.icon_info,
        destination = About,
    ),
)
