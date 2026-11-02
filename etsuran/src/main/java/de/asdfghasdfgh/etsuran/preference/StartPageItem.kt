package de.asdfghasdfgh.etsuran.preference

import kotlinx.serialization.Serializable

/** A user-curated tile on the built-in start page. */
@Serializable
data class StartPageItem(val title: String, val url: String)
