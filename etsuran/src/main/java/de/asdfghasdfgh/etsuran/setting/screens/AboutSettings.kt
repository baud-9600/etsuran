package de.asdfghasdfgh.etsuran.setting.screens

import de.asdfghasdfgh.etsuran.setting.LinkSettingItem
import de.asdfghasdfgh.etsuran.setting.SettingItemInterface

fun buildAboutSettingItems(deps: SettingScreenDeps): List<SettingItemInterface> =
    LinkSettingItem.entries.filter { it != LinkSettingItem.Manual }
