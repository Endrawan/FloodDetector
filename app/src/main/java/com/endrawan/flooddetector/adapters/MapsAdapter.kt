package com.endrawan.flooddetector.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.endrawan.flooddetector.R
import com.endrawan.flooddetector.models.Device
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.item_maps.view.*

class MapsAdapter(val mapboxMap: MapboxMap, val devices: List<Device>, val action: Action) :
    RecyclerView.Adapter<MapsAdapter.MapsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapsViewHolder =
        MapsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_maps, parent, false), action
        )

    override fun onBindViewHolder(holder: MapsViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int = devices.size

    class MapsViewHolder(val view: View, val action: Action) : RecyclerView.ViewHolder(view) {
        val type = view.type
        val latitude = view.latitude
        val longitude = view.longitude

        fun bind(device: Device) {
            type.text = device.type
            latitude.text = "Lat: ${device.latitude}"
            longitude.text = "Long: ${device.longitude}"
            view.setOnClickListener {
                action.clicked(device)
            }
        }
    }

    interface Action {
        fun clicked(device: Device)
    }
}