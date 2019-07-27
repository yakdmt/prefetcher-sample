package xyz.yakdmt.prefetcher

import timber.log.Timber

internal object RecyclerPrefetchingLogger {

    private const val LOGGING_ENABLED = true

    inline fun log(text: () -> String) {
        if (LOGGING_ENABLED && BuildConfig.DEBUG) {
            Timber.d(text())
        }
    }

}