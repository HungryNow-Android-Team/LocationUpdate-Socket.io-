package com.deevvdd.rider

import android.app.Application
import timber.log.Timber

/**
 * Created by heinhtet deevvdd@gmail.com on 15,August,2021
 */
class RiderApp : Application(){

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}