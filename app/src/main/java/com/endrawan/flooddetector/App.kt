package com.endrawan.flooddetector

import android.app.Application
import android.content.res.Resources
import com.mapbox.mapboxsdk.Mapbox

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        resourses = resources
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
    }

    companion object {
        var instance: App? = null
            private set
        var resourses: Resources? = null
            private set
    }

}