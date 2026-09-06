package com.mwickes.gratuitinator.navigation

import android.net.Uri

/**
 * Navigable destinations. Split is intentionally not wired in yet (deferred, lower priority
 * than receipt scanning); Review is reachable only from Scan, never a bottom-nav tab.
 */
sealed interface Destination {
    val route: String

    data object Main : Destination {
        override val route = "main"
    }

    data object Scan : Destination {
        override val route = "scan"
    }

    data object Review : Destination {
        const val ARG_URI = "uri"
        override val route = "review/{$ARG_URI}"

        /** Builds a concrete route for navigating to Review with a captured image [uri]. */
        fun route(uri: Uri): String = "review/${Uri.encode(uri.toString())}"
    }
}
