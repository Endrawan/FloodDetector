package com.endrawan.flooddetector.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.endrawan.flooddetector.R
import com.endrawan.flooddetector.adapters.DevicesAdapter
import com.endrawan.flooddetector.helper.Dummies
import com.endrawan.flooddetector.models.Device
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_recycler_view_on_map.*

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class DevicesFragment : Fragment() {

    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = DevicesAdapter(Dummies.Devices, object : DevicesAdapter.Action {
            override fun itemClicked(device: Device) {
                startActivity(
                    Intent(activity, DetailActivity::class.java).apply {
                        putExtra("DEVICE", gson.toJson(device))
                    }
                )
            }

        })
    }
}