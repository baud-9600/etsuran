package de.asdfghasdfgh.etsuran.view.dialog.compose

import androidx.compose.runtime.Composable
import de.asdfghasdfgh.etsuran.activity.GptActionDialog
import de.asdfghasdfgh.etsuran.preference.ChatGPTActionInfo
import de.asdfghasdfgh.etsuran.preference.GptActionType

class ShowEditGptActionDialogFragment(
    private val editActionIndex: Int = -1,
) : ComposeDialogFragment() {
    @Composable
    override fun Content() {
        var actionList = config.ai.gptActionList
        GptActionDialog(
            editActionIndex,
            if (editActionIndex >= 0) actionList[editActionIndex] else createDefaultGptAction(),
            config.ai.getGptTypeModelMap(),
            okAction = { modifiedAction ->
                actionList = actionList.toMutableList().apply {
                    if (editActionIndex >= 0) set(editActionIndex, modifiedAction)
                    else add(modifiedAction)
                }
                config.ai.gptActionList = actionList
                dismiss()
            },
            dismissAction = { dismiss() }
        )
    }

    private fun createDefaultGptAction(): ChatGPTActionInfo {
        return ChatGPTActionInfo(
            "",
            "",
            "",
            GptActionType.Default,
            config.ai.getDefaultActionModel()
        )
    }
}