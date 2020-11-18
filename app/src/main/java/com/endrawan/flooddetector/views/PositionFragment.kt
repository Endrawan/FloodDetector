package com.endrawan.flooddetector.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.endrawan.flooddetector.R
import com.endrawan.flooddetector.adapters.DevicesAdapter
import com.endrawan.flooddetector.helper.Dummies
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField
import kotlinx.android.synthetic.main.fragment_position.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


/**
 * A simple [Fragment] subclass.
 * Use the [PositionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PositionFragment : Fragment(), OnMapReadyCallback, PermissionsListener {

    private lateinit var permissionsManager: PermissionsManager
    private lateinit var mapboxMap: MapboxMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_position, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        this.mapboxMap.uiSettings.setAllGesturesEnabled(false)
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            changeMapLanguage(it)
            enableLocationComponent(it)
            initRecyclerView()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
            val locationComponent = mapboxMap.locationComponent
            locationComponent.apply {
                activateLocationComponent(
                    LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle)
                        .build()
                )
                isLocationComponentEnabled = true
                cameraMode = CameraMode.TRACKING
                renderMode = RenderMode.COMPASS
            }
            val location = locationComponent.lastKnownLocation
            if (location != null) {
                retrieveLocation(Point.fromLngLat(location.longitude, location.latitude))
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(activity)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(requireContext(), "Needs Explanation!", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            mapboxMap.getStyle { style -> enableLocationComponent(style) }
        } else {
            Toast.makeText(requireContext(), "Permissions Not Granted!", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun changeMapLanguage(style: Style) {
        val mapText = style.getLayer("country-label")
        mapText?.setProperties(textField("{name_id}"))
    }

    private fun initRecyclerView() {
        updateTextViewDescription(Dummies.Devices.size)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = DevicesAdapter(Dummies.Devices)
    }

    private fun retrieveLocation(location: Point) {
        val reverseGeocode = MapboxGeocoding.builder()
            .accessToken(getString(R.string.mapbox_access_token))
            .query(location)
            .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
            .languages("id-ID")
            .build()

        reverseGeocode.enqueueCall(object : Callback<GeocodingResponse> {
            override fun onResponse(
                call: Call<GeocodingResponse>,
                response: Response<GeocodingResponse>
            ) {
                Timber.d("Response code: ${response.code()}")
                val body = response.body()
                if (body == null) {
                    Timber.e("No body found.")
                    return
                }
                val features = body.features()
                if (features.size > 0) {
                    val address: String? = features[0].placeName()
                    Timber.d("onResponse: $address")
                    updateTextViewLocation(address)
                } else {
                    Timber.d("onResponse: No result found")
                }
            }

            override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }

    private fun updateTextViewLocation(address: String?) {
        city.text = address
    }

    private fun updateTextViewDescription(length: Int) {
        deviceCountDesc.text = "Terdapat $length buah titik banjir di lokasi anda: "
    }

    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}