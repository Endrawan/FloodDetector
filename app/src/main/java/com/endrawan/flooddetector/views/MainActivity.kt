package com.endrawan.flooddetector.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.endrawan.flooddetector.R
import com.endrawan.flooddetector.internal.Extension.makeStatusBarTransparent
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val devicesFragment = DevicesFragment()
    private val mapsFragment = MapsFragment()
    private val positionFragment = PositionFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }

    private fun changeFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }
}