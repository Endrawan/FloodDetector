package com.endrawan.flooddetector.helper

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.endrawan.flooddetector.R
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_recycler_view_on_map.*
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewOnMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private val SYMBOL_ICON_ID = "SYMBOL_ICON_ID"
    private val SOURCE_ID = "SOURCE_ID"
    private val LAYER_ID = "LAYER_ID"
    lateinit var mapboxMap: MapboxMap
    private lateinit var featureCollection: FeatureCollection

    private val coordinates = arrayListOf<LatLng>(
        LatLng(-34.6054099, -58.363654800000006),
        LatLng(-34.6041508, -58.38555650000001),
        LatLng(-34.6114412, -58.37808899999999),
        LatLng(-34.6097604, -58.382064000000014),
        LatLng(-34.596636, -58.373077999999964),
        LatLng(-34.590548, -58.38256609999996),
        LatLng(-34.5982127, -58.38110440000003)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called in your application
        // object or in the same activity which contains the mapview
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured
        setContentView(R.layout.activity_recycler_view_on_map)

        // Initialize the map view
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this@RecyclerViewOnMapActivity.mapboxMap = mapboxMap
        mapboxMap.setStyle(
            Style.DARK
        ) {
            initFeatureCollection()
            initMarkerIcons(it)
            initRecyclerView()
            Toast.makeText(this@RecyclerViewOnMapActivity, "Howw lewwdd!", Toast.LENGTH_SHORT)
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

    private fun initFeatureCollection() {
        val featureList = mutableListOf<Feature>()
        for (latLng in coordinates) {
            featureList.add(
                Feature.fromGeometry(
                    Point.fromLngLat(
                        latLng.longitude,
                        latLng.latitude
                    )
                )
            )
        }
        featureCollection = FeatureCollection.fromFeatures(featureList)
    }

    private fun initRecyclerView() {
        val locationAdapter = LocationRecyclerViewAdapter(createRecyclerViewLocations(), mapboxMap)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = locationAdapter
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
    }

    private fun initMarkerIcons(loadedMapStyle: Style) {
        loadedMapStyle.addImage(
            SYMBOL_ICON_ID, BitmapFactory.decodeResource(
                this.resources, R.drawable.red_marker
            )
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

    private fun createRecyclerViewLocations(): List<SingleRecyclerViewLocation> {
        val locationList: ArrayList<SingleRecyclerViewLocation> = ArrayList()
        for (x in coordinates.indices) {
            val singleLocation = SingleRecyclerViewLocation()
            singleLocation.name = String.format(getString(R.string.rv_card_name), x)
            singleLocation.bedInfo =
                java.lang.String.format(
                    getString(R.string.rv_card_bed_info),
                    Random().nextInt(coordinates.size)
                )
            singleLocation.locationCoordinates = coordinates[x]
            locationList.add(singleLocation)
        }
        return locationList
    }

    /**
     * POJO model class for a single location in the recyclerview
     */
    data class SingleRecyclerViewLocation(
        var name: String?,
        var bedInfo: String?,
        var locationCoordinates: LatLng?
    ) {
        constructor() : this(null, null, null)
    }

    class LocationRecyclerViewAdapter
        (val locationList: List<SingleRecyclerViewLocation>, val mapBoxMap: MapboxMap) :
        RecyclerView.Adapter<LocationRecyclerViewAdapter.MyViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): LocationRecyclerViewAdapter.MyViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_on_top_of_map_card, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(
            holder: LocationRecyclerViewAdapter.MyViewHolder,
            position: Int
        ) {
            val singleRecyclerViewLocation = locationList[position]
            holder.name.text = singleRecyclerViewLocation.name
            holder.numOfBeds.text = singleRecyclerViewLocation.bedInfo
            holder.setActionListener(object : ItemClickListener {
                override fun onClick(view: View, position: Int) {
                    val selectedLocationLatLng = locationList[position].locationCoordinates
                    val newCameraPosition =
                        CameraPosition.Builder().target(selectedLocationLatLng).build()
                    mapBoxMap.easeCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
                }
            })
        }

        override fun getItemCount(): Int = locationList.size

        class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
            val name = itemView.findViewById<TextView>(R.id.location_title_tv)
            val numOfBeds = itemView.findViewById<TextView>(R.id.location_num_of_beds_tv)
            val singleCard = itemView.findViewById<CardView>(R.id.single_location_cardview).apply {
                setOnClickListener(this@MyViewHolder)
            }
            lateinit var clickListener: ItemClickListener

            fun setActionListener(itemClickListener: ItemClickListener) {
                this.clickListener = itemClickListener
            }

            override fun onClick(view: View) {
                clickListener.onClick(view, layoutPosition)
            }

        }

        interface ItemClickListener {
            fun onClick(view: View, position: Int)
        }

    }
}