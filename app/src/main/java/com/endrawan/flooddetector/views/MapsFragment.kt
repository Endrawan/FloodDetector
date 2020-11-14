package com.endrawan.flooddetector.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.endrawan.flooddetector.R
import com.endrawan.flooddetector.adapters.MapsAdapter
import com.endrawan.flooddetector.helper.Dummies
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.fragment_maps.*

class MapsFragment : Fragment(), OnMapReadyCallback {

    private val SYMBOL_ICON_ID = "SYMBOL_ICON_ID"
    private val SOURCE_ID = "SOURCE_ID"
    private val LAYER_ID = "LAYER_ID"
    private val data = Dummies.Devices
    private lateinit var featureCollection: FeatureCollection

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
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            initFeatureCollection()
            initMarkerIcons(it)
            initRecyclerView()
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
        recyclerView.adapter = MapsAdapter(data)
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
    }
}