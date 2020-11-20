package com.endrawan.flooddetector.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.endrawan.flooddetector.R
import com.endrawan.flooddetector.internal.Extension.makeStatusBarTransparent
import com.endrawan.flooddetector.models.Device
import com.google.gson.Gson
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class DetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private val SOURCE_ID = "SOURCE_ID"
    private val ICON_ID = "ICON_ID"
    private val LAYER_ID = "LAYER_ID"
    private lateinit var mapboxMap: MapboxMap

    private lateinit var device: Device
    private var location = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        makeStatusBarTransparent()
        getDataFromActivity()
        updateView()
        retrieveLocation(Point.fromLngLat(device.longitude, device.latitude))
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        this.mapboxMap.uiSettings.setAllGesturesEnabled(false)
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            showMapAndIcon(it)
        }
    }

    private fun showMapAndIcon(style: Style) {
        val symbolLayerIconFeatureList = arrayListOf(
            Feature.fromGeometry(Point.fromLngLat(device.longitude, device.latitude))
        )
        style.apply {
            val drawable = ContextCompat.getDrawable(this@DetailActivity, R.drawable.ic_device)
            addImage(ICON_ID, drawable!!)
            addSource(
                GeoJsonSource(
                    SOURCE_ID,
                    FeatureCollection.fromFeatures(symbolLayerIconFeatureList)
                )
            )
            addLayer(
                SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
                    iconImage(ICON_ID),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true)
                )
            )
        }
        changeCameraLocation(LatLng(device.latitude, device.longitude))
    }

    private fun changeCameraLocation(latLng: LatLng) {
        val position = CameraPosition.Builder()
            .target(latLng)
            .zoom(10.0)
            .build()
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000)
    }

    private fun getDataFromActivity() {
        val deviceJson = intent.getStringExtra("DEVICE")
        val gson = Gson()
        device = gson.fromJson(deviceJson, Device::class.java)
    }

    private fun updateView() {
        deviceName.text = device.name
        deviceDescription.text =
            "ID: ${device.ID}\nLatitude: ${device.latitude}\nLongitude: ${device.longitude}"
        deviceLocation.text = location
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
                    this@DetailActivity.location = address!!
                    updateView()
                } else {
                    Timber.d("onResponse: No result found")
                }
            }

            override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}