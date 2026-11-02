package de.asdfghasdfgh.etsuran.setting.screens

import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.setting.ActionSettingItem
import de.asdfghasdfgh.etsuran.setting.BooleanSettingItem
import de.asdfghasdfgh.etsuran.setting.SettingItemInterface

fun buildClearDataSettingItems(deps: SettingScreenDeps): List<SettingItemInterface> {
    val config = deps.config
    return listOf(
        BooleanSettingItem(
            R.string.clear_title_cache,
            0,
            config = config::clearCache,
        ),
        BooleanSettingItem(
            R.string.clear_title_history,
            0,
            config = config::clearHistory,
        ),
        BooleanSettingItem(
            R.string.clear_title_indexedDB,
            0,
            config = config::clearIndexedDB,
        ),
        BooleanSettingItem(
            R.string.clear_title_cookie,
            0,
            R.string.setting_summary_cookie_delete,
            config::clearCookies
        ),
        BooleanSettingItem(
            R.string.clear_title_quit,
            0,
            R.string.clear_summary_quit,
            config::clearWhenQuit
        ),
        ActionSettingItem(
            R.string.clear_title_deleteDatabase,
            0,
            R.string.clear_summary_deleteDatabase,
        ) {
            deps.activity.deleteDatabase("Ninja4.db")
            deps.activity.deleteDatabase("pass_DB_v01.db")
            config.restartChanged = true
            deps.activity.finish()
        }
    )
}
