package de.asdfghasdfgh.etsuran.task

import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.task.tasks.ReadArticleListTask

/**
 * Registry of built-in task templates. Add new entries here and they appear
 * automatically in the task picker dialog.
 *
 * Free-form / custom tasks are NOT in this catalog — they are constructed on the
 * fly from a user prompt and handled separately by BrowserActivity.dispatch().
 */
object TaskCatalog {
    val builtIns: List<TaskDescriptor> = listOf(
        TaskDescriptor(
            id = "read_article_list",
            displayNameResId = R.string.task_read_article_list,
            descriptionResId = R.string.task_read_article_list_desc,
        ) { ReadArticleListTask() },
    )

    fun byId(id: String): TaskDescriptor? = builtIns.firstOrNull { it.id == id }
}

data class TaskDescriptor(
    val id: String,
    val displayNameResId: Int,
    val descriptionResId: Int,
    val factory: () -> BrowserTask,
)
