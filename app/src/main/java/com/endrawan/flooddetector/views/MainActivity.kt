package com.endrawan.flooddetector.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.endrawan.flooddetector.R
import com.endrawan.flooddetector.internal.Extension.makeStatusBarTransparent
import com.endrawan.flooddetector.models.Device
import com.endrawan.flooddetector.services.FirebaseBackgroundService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val firebaseReference = FirebaseDatabase.getInstance().reference
    val devicesLiveData = MutableLiveData<List<Device>>()

    private val devicesFragment = DevicesFragment()
    private val mapsFragment = MapsFragment()
    private val positionFragment = PositionFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        startService(Intent(FirebaseBackgroundService::class.java.name))
        Intent(this, FirebaseBackgroundService::class.java).also {
            startService(it)
        }

        makeStatusBarTransparent()
        changeFragment(devicesFragment)

        bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.devices -> {
                    changeFragment(devicesFragment)
                    true
                }
                R.id.maps_status -> {
                    changeFragment(mapsFragment)
                    true
                }
                R.id.current_position -> {
                    changeFragment(positionFragment)
                    true
                }
                else -> false
            }
        }

        retrieveDataFromFirebase()
    }

    private fun changeFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    private fun retrieveDataFromFirebase() {
        val endpoint = firebaseReference.child("devices")
        endpoint.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val devices = mutableListOf<Device>()
                dataSnapshot.children.mapNotNullTo(devices) {
                    val device = it.getValue(Device::class.java)!!
                    device.ID = it.key!!
                    device
                }
                devicesLiveData.value = devices
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.e(databaseError.details)
            }

        })
    }
}