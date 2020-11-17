package com.endrawan.flooddetector.models

data class Device(
    val ID: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val height: Double
)