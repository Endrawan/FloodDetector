package com.endrawan.flooddetector.helper

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.endrawan.flooddetector.R
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_directions.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

/**
 * Use Mapbox Java Services to request directions from the Mapbox Directions API and show the
 * route with a LineLayer.
 */

class DirectionsActivity : AppCompatActivity() {

    private val ROUTE_LAYER_ID = "route-layer-id"
    private val ROUTE_SOURCE_ID = "route-source-id"
    private val ICON_LAYER_ID = "icon-layer-id"
    private val ICON_SOURCE_ID = "icon-source-id"
    private val RED_PIN_ICON_ID = "red-pin-icon-id"

    private lateinit var currentRoute: DirectionsRoute
    private lateinit var client: MapboxDirections
    private lateinit var origin: Point
    private lateinit var destination: Point

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_directions)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                // Set the origin location to the Alhambra landmark in Granada, Spain.
                origin = Point.fromLngLat(-3.588098, 37.176164)

                // Set the destination to the Plaza del Triunfo in Granada, Spain
                destination = Point.fromLngLat(-3.601845, 37.184080)

                initSource(style)

                initLayers(style)

                // Get the directions route from the Mapbox Directions API
                getRoute(mapboxMap, origin, destination)
            }
        }
    }

    /**
     * Add the route and marker sources to the map
     */
    private fun initSource(loadedMapStyle: Style) {
        loadedMapStyle.addSource(GeoJsonSource(ROUTE_SOURCE_ID))

        val iconGeoJsonSource = GeoJsonSource(
            ICON_SOURCE_ID,
            FeatureCollection.fromFeatures(
                listOf(
                    Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude())),
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            destination.longitude(),
                            destination.latitude()
                        )
                    )
                )
            )
        )

        loadedMapStyle.addSource(iconGeoJsonSource)
    }

    /**
     * Add the route and marker icon layers to the map
     */
    private fun initLayers(loadedMapStyle: Style) {
        val routeLayer = LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)

        // Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
            lineCap(Property.LINE_CAP_ROUND),
            lineJoin(Property.LINE_JOIN_ROUND),
            lineWidth(5f),
            lineColor(Color.parseColor("#009688"))
        )
        loadedMapStyle.addLayer(routeLayer)

        // Add the red marker icon image to the map
        loadedMapStyle.addImage(
            RED_PIN_ICON_ID, BitmapFactory.decodeResource(
                this.resources, R.drawable.red_marker
            )
        )

        // Add the red marker icon SymbolLayer to the map
        loadedMapStyle.addLayer(
            SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID)
                .withProperties(
                    iconImage(RED_PIN_ICON_ID),
                    iconIgnorePlacement(true),
                    iconAllowOverlap(true),
                    iconOffset(arrayOf(0f, -9f))
                )
        )
    }

    /**
     * Make a request to the Mapbox Directions API. Once successful, pass the route to the
     * route layer.
     * @param mapboxMap the Mapbox map object that the route will be drawn on
     * @param origin      the starting point of the route
     * @param destination the desired finish point of the route
     */
    private fun getRoute(mapboxMap: MapboxMap, origin: Point, destination: Point) {
        client = MapboxDirections.builder()
            .origin(origin)
            .destination(destination)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .accessToken(getString(R.string.mapbox_access_token))
            .build()

        client.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                // You can get the generic HTTP info about the response
                Timber.d("Response code: ${response.code()}")
                val body = response.body()
                if (body == null) {
                    Timber.e("No routes found, make sure you set the right user and access token.")
                    return
                }
                val routes = body.routes()
                if (routes.size < 1) {
                    Timber.e("No routes found")
                    return
                }

                // Get the directions route
                currentRoute = routes[0]

                // Make a toast which displays the route's distance
                Toast.makeText(
                    this@DirectionsActivity, "Distance: ${currentRoute.distance()}",
                    Toast.LENGTH_SHORT
                ).show()

                if (mapboxMap != null) {
                    mapboxMap.getStyle {
                        // Retrieve and update the source designated for showing the
                        // directions route
                        val source = it.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID)

                        // Create a LineString with the directions route's geometry and
                        // reset the GeoJSON source for the route LineLayer source
                        source?.setGeoJson(
                            LineString.fromPolyline(
                                currentRoute.geometry()!!,
                                PRECISION_6
                            )
                        )
                    }
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Timber.e("Error: ${t.message}")
                Toast.makeText(
                    this@DirectionsActivity, "Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the Directions API request
        if (client != null) {
            client.cancelCall()
        }
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}