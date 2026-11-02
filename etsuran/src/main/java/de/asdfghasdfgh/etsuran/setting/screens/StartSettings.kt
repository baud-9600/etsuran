package de.asdfghasdfgh.etsuran.setting.screens

import android.content.Intent
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.activity.AdBlockSettingActivity
import de.asdfghasdfgh.etsuran.activity.DataListActivity
import de.asdfghasdfgh.etsuran.activity.SiteRuleListActivity
import de.asdfghasdfgh.etsuran.activity.UserScriptListActivity
import de.asdfghasdfgh.etsuran.activity.WhiteListType
import de.asdfghasdfgh.etsuran.setting.ActionSettingItem
import de.asdfghasdfgh.etsuran.setting.BooleanSettingItem
import de.asdfghasdfgh.etsuran.setting.DividerSettingItem
import de.asdfghasdfgh.etsuran.setting.ListSettingWithEnumItem
import de.asdfghasdfgh.etsuran.setting.SettingItemInterface

fun buildStartSettingItems(deps: SettingScreenDeps): List<SettingItemInterface> {
    val config = deps.config
    return listOf(
        ActionSettingItem(
            R.string.setting_title_site_rules,
            0,
            R.string.setting_summary_site_rules,
        ) { deps.activity.startActivity(SiteRuleListActivity.createIntent(deps.activity)) },
        DividerSettingItem(),
        BooleanSettingItem(
            R.string.setting_title_images,
            0,
            R.string.setting_summary_images,
            config.browser::enableImages
        ),
        BooleanSettingItem(
            R.string.setting_title_auto_fill_form,
            0,
            R.string.setting_summary_auto_fill_form,
            config.browser::autoFillForm
        ),
        ListSettingWithEnumItem(
            R.string.setting_title_history,
            0,
            R.string.setting_summary_history,
            config.tab::saveHistoryMode,
            listOf(
                R.string.save_history_mode_save_when_open,
                R.string.save_history_mode_save_when_close,
                R.string.save_history_mode_disabled,
            )
        ),
        BooleanSettingItem(
            R.string.setting_title_debug,
            0,
            R.string.setting_summary_debug,
            config.browser::debugWebView
        ),
        BooleanSettingItem(
            R.string.setting_title_remote,
            0,
            R.string.setting_summary_remote,
            config.browser::enableRemoteAccess
        ),
        BooleanSettingItem(
            R.string.setting_title_location,
            0,
            R.string.setting_summary_location,
            config.browser::shareLocation
        ),
        DividerSettingItem(),
        BooleanSettingItem(
            R.string.setting_title_adblock,
            0,
            R.string.setting_summary_adblock,
            config.browser::adBlock
        ),
        ActionSettingItem(
            R.string.setting_title_update_adblock,
            0,
            R.string.setting_summary_update_adblock,
        ) {
            deps.activity.startActivity(Intent(deps.activity, AdBlockSettingActivity::class.java))
            deps.activity.finish()
        },
        ActionSettingItem(
            R.string.setting_title_whitelist,
            0,
            R.string.setting_summary_whitelist,
        ) {
            deps.activity.startActivity(
                DataListActivity.createIntent(deps.activity, WhiteListType.Adblock)
            )
        },
        DividerSettingItem(),
        BooleanSettingItem(
            R.string.setting_title_javascript,
            0,
            R.string.setting_summary_javascript,
            config.browser::enableJavascript
        ),
        ActionSettingItem(
            R.string.setting_title_whitelistJS,
            0,
            R.string.setting_summary_whitelistJS,
        ) {
            deps.activity.startActivity(
                DataListActivity.createIntent(deps.activity, WhiteListType.Javascript)
            )
        },
        ActionSettingItem(
            R.string.setting_title_userscripts,
            0,
            R.string.setting_summary_userscripts,
        ) { deps.activity.startActivity(UserScriptListActivity.createIntent(deps.activity)) },
        DividerSettingItem(),
        BooleanSettingItem(
            R.string.setting_title_cookie,
            0,
            R.string.setting_summary_cookie,
            config.browser::cookies
        ),
        ActionSettingItem(
            R.string.setting_title_whitelistCookie,
            0,
            R.string.setting_summary_whitelistCookie,
        ) {
            deps.activity.startActivity(
                DataListActivity.createIntent(deps.activity, WhiteListType.Cookie)
            )
        },
        DividerSettingItem(),
        BooleanSettingItem(
            R.string.setting_title_save_data,
            0,
            R.string.setting_summary_save_data,
            config.browser::enableSaveData
        ),
    )
}
