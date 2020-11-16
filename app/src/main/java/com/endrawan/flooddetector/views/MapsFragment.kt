package com.endrawan.flooddetector.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.endrawan.flooddetector.R
import com.endrawan.flooddetector.adapters.MapsAdapter
import com.endrawan.flooddetector.helper.Dummies
import com.endrawan.flooddetector.models.Device
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.fragment_maps.*
import java.lang.ref.WeakReference

class MapsFragment : Fragment(), OnMapReadyCallback, PermissionsListener {

    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    private val SYMBOL_ICON_ID = "SYMBOL_ICON_ID"
    private val SOURCE_ID = "SOURCE_ID"
    private val LAYER_ID = "LAYER_ID"

    private val data = Dummies.Devices

    private lateinit var featureCollection: FeatureCollection
    private lateinit var mapboxMap: MapboxMap
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationEngine: LocationEngine
    private var callback = LocationChangeMapsFragmentLocationCallback(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this@MapsFragment.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            initFeatureCollection()
            initMarkerIcons(it)
            initRecyclerView()
            enableLocationComponent(it)
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(requireContext(), "Needs explanation!", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            mapboxMap.getStyle {
                enableLocationComponent(it)
            }
        } else {
            Toast.makeText(requireContext(), "Permissions tidak diizinkan!", Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback)
        }
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    private fun initFeatureCollection() {
        val featureList = mutableListOf<Feature>()
        for (d in data) {
            featureList.add(
                Feature.fromGeometry(Point.fromLngLat(d.longitude, d.latitude))
            )
        }
        featureCollection = FeatureCollection.fromFeatures(featureList)
    }

    private fun initMarkerIcons(loadedMapStyle: Style) {
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_device)
        loadedMapStyle.addImage(
            SYMBOL_ICON_ID, drawable!!
        )
        loadedMapStyle.addSource(GeoJsonSource(SOURCE_ID, featureCollection))
        loadedMapStyle.addLayer(
            SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
                iconImage(SYMBOL_ICON_ID),
                iconAllowOverlap(true),
                iconOffset(arrayOf(0f, -4f))
            )
        )
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = MapsAdapter(mapboxMap, data, object : MapsAdapter.Action {
            override fun clicked(device: Device) {
                val latLng = LatLng(device.latitude, device.longitude)
                val newCameraPosition =
                    CameraPosition.Builder().target(latLng).build()
                mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
            }

        })
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        val context = requireContext()
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            val locationComponent = mapboxMap.locationComponent
            val locationComponentActivationOptions = LocationComponentActivationOptions
                .builder(context, loadedMapStyle)
                .useDefaultLocationEngine(false)
                .build()
            locationComponent.apply {
                activateLocationComponent(locationComponentActivationOptions)
                isLocationComponentEnabled = true
                cameraMode = CameraMode.TRACKING
                renderMode = RenderMode.COMPASS
            }
            initLocationEngine()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(activity)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext())
        val request = LocationEngineRequest
            .Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()

        locationEngine.apply {
            requestLocationUpdates(request, callback, activity?.mainLooper)
            getLastLocation(callback)
        }
    }

    private class LocationChangeMapsFragmentLocationCallback(fragment: MapsFragment) :
        LocationEngineCallback<LocationEngineResult> {

        private val fragmentWeakReference: WeakReference<MapsFragment> = WeakReference(fragment)

        override fun onSuccess(result: LocationEngineResult) {
            val fragment = fragmentWeakReference.get()
            if (fragment != null) {
                val location = result.lastLocation ?: return

                // Create a Toast which displays the new location's coordinates
                Toast.makeText(
                    fragment.activity,
                    "New location-> lat:${result.lastLocation?.latitude}, long: ${result.lastLocation?.longitude}",
                    Toast.LENGTH_SHORT
                ).show()

                // Pass the new location to the Maps SDK's LocationComponent
                if (fragment.mapboxMap != null && result.lastLocation != null) {
                    fragment.mapboxMap.locationComponent.forceLocationUpdate(result.lastLocation)
                }
            }
        }

        override fun onFailure(exception: Exception) {
            val fragment = fragmentWeakReference.get()
            if (fragment != null) {
                Toast.makeText(
                    fragment.activity, exception.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}