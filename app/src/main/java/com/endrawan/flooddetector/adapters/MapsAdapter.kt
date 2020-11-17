package com.endrawan.flooddetector.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.endrawan.flooddetector.R
import com.endrawan.flooddetector.models.Device
import kotlinx.android.synthetic.main.item_maps.view.*

class MapsAdapter(private val devices: List<Device>, private val action: Action) :
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

    class MapsViewHolder(view: View, private val action: Action) : RecyclerView.ViewHolder(view) {
        val type = view.type
        val latitude = view.latitude
        val longitude = view.longitude
        val location = view.show_location
        val directions = view.show_directions

        fun bind(device: Device) {
            type.text = device.type
            val lat = String.format("%.5f", device.latitude).toDouble()
            val long = String.format("%.5f", device.longitude).toDouble()
            latitude.text = "Lat: $lat"
            longitude.text = "Long: $long"
            location.setOnClickListener { action.locationClicked(device) }
            directions.setOnClickListener { action.directionsClicked(device) }
        }
    }

    interface Action {
        fun locationClicked(device: Device)
        fun directionsClicked(device: Device)
    }
}