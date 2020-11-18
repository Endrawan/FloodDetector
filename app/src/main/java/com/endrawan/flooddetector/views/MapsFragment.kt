package com.endrawan.flooddetector.views

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
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
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.fragment_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.lang.ref.WeakReference

class MapsFragment : Fragment(), OnMapReadyCallback, PermissionsListener {

    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    private val DEVICES_ICON_ID = "SYMBOL_ICON_ID"
    private val DEVICES_SOURCE_ID = "SOURCE_ID"
    private val DEVICES_LAYER_ID = "LAYER_ID"
    private val ROUTE_LAYER_ID = "route-layer-id"
    private val ROUTE_SOURCE_ID = "route-source-id"
    private val TAG = "MapsFragment"

    private val data = Dummies.Devices

    private lateinit var featureCollection: FeatureCollection
    private lateinit var mapboxMap: MapboxMap
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationEngine: LocationEngine
    private lateinit var currentRoute: DirectionsRoute
    private lateinit var client: MapboxDirections
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
            changeMapLanguage(it)
            initFeatureCollection()
            initMarkerIcons(it)
            initRoutesSource(it)
            initRoutesLayer(it)
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
        locationEngine.removeLocationUpdates(callback)
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

    private fun changeMapLanguage(style: Style) {
        val mapText = style.getLayer("country-label")
        mapText?.setProperties(textField("{name_id}"))
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
            DEVICES_ICON_ID, drawable!!
        )
        loadedMapStyle.addSource(GeoJsonSource(DEVICES_SOURCE_ID, featureCollection))
        loadedMapStyle.addLayer(
            SymbolLayer(DEVICES_LAYER_ID, DEVICES_SOURCE_ID).withProperties(
                iconImage(DEVICES_ICON_ID),
                iconAllowOverlap(true),
                iconOffset(arrayOf(0f, -4f))
            )
        )
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = MapsAdapter(data, object : MapsAdapter.Action {

            override fun locationClicked(device: Device) {
                val latLng = LatLng(device.latitude, device.longitude)
                val newCameraPosition =
                    CameraPosition.Builder().target(latLng).zoom(mapboxMap.cameraPosition.zoom)
                        .build()
                mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
            }

            override fun directionsClicked(device: Device) {
                Toast.makeText(activity, "Directions Clicked!", Toast.LENGTH_SHORT).show()
                val lastLocation = mapboxMap.locationComponent.lastKnownLocation
                if (lastLocation == null) {
                    Toast.makeText(
                        activity,
                        "Lokasi anda tidak dapat ditemukan, silahkan coba lagi!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val origin = Point.fromLngLat(lastLocation.longitude, lastLocation.latitude)
                    val destination = Point.fromLngLat(device.longitude, device.latitude)
                    Log.d(TAG, "origin: $origin, destination: $destination")
                    getRoute(mapboxMap, origin, destination)
                }
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
                // Pass the new location to the Maps SDK's LocationComponent
                if (result.lastLocation != null) {
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

    private fun initRoutesLayer(loadedMapStyle: Style) {
        val routeLayer = LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)
        routeLayer.setProperties(
            lineCap(Property.LINE_CAP_ROUND),
            lineJoin(Property.LINE_JOIN_ROUND),
            lineWidth(5f),
            lineColor(Color.parseColor("#009688"))
        )
        loadedMapStyle.addLayer(routeLayer)
    }

    private fun initRoutesSource(loadedMapStyle: Style) {
        loadedMapStyle.addSource(GeoJsonSource(ROUTE_SOURCE_ID))
    }

    private fun getRoute(mapboxMap: MapboxMap, origin: Point, destination: Point) {
        client = MapboxDirections.builder()
            .origin(origin)
            .destination(destination)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .accessToken(getString(R.string.mapbox_access_token))
            .build()

        client.enqueueCall(object : Callback<DirectionsResponse> {
            //            @SuppressLint("LogNotTimber")
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
//                Log.d(TAG, "Response code: ${response.code()}")
                Timber.d("Response code: ${response.code()}")
                val body = response.body()
                if (body == null) {
//                    Log.d(TAG, "No routes found, make sure you set the right user and access token.")
                    Timber.e("No routes found, make sure you set the right user and access token.")
                    return
                }
                val routes = body.routes()
                if (routes.size < 1) {
//                    Log.d(TAG, "No routes found")
                    Timber.e("No routes found")
                    return
                }

                currentRoute = routes[0]
                Toast.makeText(
                    requireContext(), "Distance: ${currentRoute.distance()}",
                    Toast.LENGTH_SHORT
                ).show()

                if (mapboxMap != null) {
                    mapboxMap.getStyle {
                        val source = it.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID)
                        source?.setGeoJson(
                            LineString.fromPolyline(
                                currentRoute.geometry()!!,
                                PRECISION_6
                            )
                        )
                    }
                }
            }

            //            @SuppressLint("LogNotTimber")
            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
//                Log.d(TAG, "Error: ${t.message}")
                Timber.e("Error: ${t.message}")
                Toast.makeText(
                    requireContext(), "Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

}