package com.endrawan.flooddetector.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.endrawan.flooddetector.R
import com.endrawan.flooddetector.configs.ImageConfig
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
        private val distance = view.distance
        private val type = view.type
        private val latLong = view.latlong
        private val image = view.image

        fun bind(device: Device) {
            distance.text = "Jarak Air: ${device.distance} cm"
            type.text = device.name
            latLong.text = "Lat: ${device.latitude}, Long: ${device.longitude}"

            if (device.status) {
                Glide.with(view.context).load(ImageConfig.deviceDanger).centerCrop()
                    .into(image)
            }

            view.setOnClickListener { action.itemClicked(device) }
        }
    }

    interface Action {
        fun itemClicked(device: Device)
    }
}