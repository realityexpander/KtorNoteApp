package com.realityexpander.ktornoteapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class NoteApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // Setup logging
        Timber.plant(Timber.DebugTree())
    }

}