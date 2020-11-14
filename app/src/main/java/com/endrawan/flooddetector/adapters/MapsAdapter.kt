package com.endrawan.flooddetector.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.endrawan.flooddetector.R
import com.endrawan.flooddetector.models.Device
import kotlinx.android.synthetic.main.item_maps.view.*

class MapsAdapter(val devices: List<Device>) :
    RecyclerView.Adapter<MapsAdapter.MapsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapsViewHolder =
        MapsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_maps, parent, false)
        )

    override fun onBindViewHolder(holder: MapsViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int = devices.size

    class MapsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val type = view.type
        val latitude = view.latitude
        val longitude = view.longitude

        fun bind(device: Device) {
            type.text = device.type
            latitude.text = "Lat: ${device.latitude}"
            longitude.text = "Long: ${device.longitude}"
        }
    }
}