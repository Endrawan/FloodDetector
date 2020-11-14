package com.endrawan.flooddetector.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.endrawan.flooddetector.R
import com.endrawan.flooddetector.models.Device
import kotlinx.android.synthetic.main.item_device.view.*

class DevicesAdapter(val devices: List<Device>) :
    RecyclerView.Adapter<DevicesAdapter.DevicesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder =
        DevicesViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_device, parent, false)
        )

    override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int = devices.size

    class DevicesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ID = view.deviceID
        val type = view.type

        fun bind(device: Device) {
            ID.text = device.ID
            type.text = device.type
        }
    }
}