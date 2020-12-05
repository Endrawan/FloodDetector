package com.endrawan.flooddetector.configs

import androidx.core.content.res.ResourcesCompat
import com.endrawan.flooddetector.App
import com.endrawan.flooddetector.R

object ImageConfig {
    val deviceDanger =
        ResourcesCompat.getDrawable(App.resourses!!, R.drawable.ic_alert_red_24dp, null)
    val deviceNormal =
        ResourcesCompat.getDrawable(App.resourses!!, R.drawable.ic_device, null)
}