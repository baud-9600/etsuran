package de.asdfghasdfgh.etsuran.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.asdfghasdfgh.etsuran.database.BookmarkManager
import de.asdfghasdfgh.etsuran.database.SavedPage
import kotlinx.coroutines.launch
import java.io.File

class SavedPageViewModel(
    private val bookmarkManager: BookmarkManager,
) : ViewModel() {

    fun getAllSavedPages() = bookmarkManager.getAllSavedPages()

    fun deleteSavedPage(savedPage: SavedPage) {
        viewModelScope.launch {
            File(savedPage.filePath).delete()
            bookmarkManager.deleteSavedPage(savedPage)
        }
    }
}
