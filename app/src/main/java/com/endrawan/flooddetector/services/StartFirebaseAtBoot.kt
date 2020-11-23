package com.endrawan.flooddetector.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StartFirebaseAtBoot : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
//        context?.startService(Intent(FirebaseBackgroundService::class.java.name))
        Intent(context, FirebaseBackgroundService::class.java).also {
            context?.startService(it)
        }
    }
}