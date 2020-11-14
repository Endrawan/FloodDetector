package com.endrawan.flooddetector

import android.app.Application
import com.mapbox.mapboxsdk.Mapbox

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
    }

}