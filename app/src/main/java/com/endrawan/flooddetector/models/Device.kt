package com.endrawan.flooddetector.models

data class Device(
    var ID: String = "",
    var name: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var height: Double = 0.0
)