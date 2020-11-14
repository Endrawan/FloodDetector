package com.endrawan.flooddetector

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val devicesFragment = DevicesFragment()
    private val mapsFragment = MapsFragment()
    private val positionFragment = PositionFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        changeFragment(devicesFragment)

        bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.devices -> {
                    Toast.makeText(applicationContext, "Devices", Toast.LENGTH_SHORT).show()
                    changeFragment(devicesFragment)
                    true
                }
                R.id.maps_status -> {
                    Toast.makeText(applicationContext, "Maps Status", Toast.LENGTH_SHORT).show()
                    changeFragment(mapsFragment)
                    true
                }
                R.id.current_position -> {
                    Toast.makeText(applicationContext, "Current Position", Toast.LENGTH_SHORT)
                        .show()
                    changeFragment(positionFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun changeFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }
}