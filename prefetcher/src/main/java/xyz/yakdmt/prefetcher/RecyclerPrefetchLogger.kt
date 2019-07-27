package xyz.yakdmt.prefetcher

import android.util.Log

internal object RecyclerPrefetchingLogger {

    private const val LOGGING_ENABLED = false

    inline fun log(text: () -> String) {
        if (LOGGING_ENABLED && BuildConfig.DEBUG) {
            Log.d("PrefetchLogger", text())
        }
    }

}