package xyz.yakdmt.prefetcherapp

import android.app.Application
import timber.log.Timber

class PrefetcherApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}

var enableImprovements = true