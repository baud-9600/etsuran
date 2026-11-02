package de.asdfghasdfgh.etsuran.setting.screens

import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.setting.BooleanSettingItem
import de.asdfghasdfgh.etsuran.setting.SettingItemInterface
import de.asdfghasdfgh.etsuran.setting.ValueSettingItem

fun buildUserAgentSettingItems(deps: SettingScreenDeps): List<SettingItemInterface> {
    val config = deps.config
    return listOf(
        BooleanSettingItem(
            R.string.setting_title_userAgent_toggle,
            0,
            R.string.setting_summary_userAgent_toggle,
            config.browser::enableCustomUserAgent
        ),
        ValueSettingItem(
            R.string.setting_title_userAgent,
            0,
            R.string.setting_summary_userAgent,
            config.browser::customUserAgent
        ),
    )
}
