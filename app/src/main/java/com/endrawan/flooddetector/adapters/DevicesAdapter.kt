package com.endrawan.flooddetector.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.endrawan.flooddetector.R
import com.endrawan.flooddetector.models.Device
import kotlinx.android.synthetic.main.item_device.view.*

class DevicesAdapter(private val devices: List<Device>, private val action: Action) :
    RecyclerView.Adapter<DevicesAdapter.DevicesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder =
        DevicesViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_device, parent, false), action
        )

    override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int = devices.size

    class DevicesViewHolder(private val view: View, private val action: Action) :
        RecyclerView.ViewHolder(view) {
        private val ID = view.deviceID
        private val type = view.type
        private val latLong = view.latlong

        fun bind(device: Device) {
            ID.text = device.ID
            type.text = device.type
            latLong.text = "Lat: ${device.latitude}, Long: ${device.longitude}"
            view.setOnClickListener { action.itemClicked(device) }
        }
    }

    interface Action {
        fun itemClicked(device: Device)
    }
}