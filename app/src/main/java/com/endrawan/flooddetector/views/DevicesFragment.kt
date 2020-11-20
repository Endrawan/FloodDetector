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
import com.endrawan.flooddetector.models.Device
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_recycler_view_on_map.*

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class DevicesFragment : Fragment() {

    private val gson = Gson()
    private lateinit var act: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        act = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        act.devicesLiveData.observe(this, {
            initRecyclerView(it)
        })
    }

    private fun initRecyclerView(devices: List<Device>) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = DevicesAdapter(devices, object : DevicesAdapter.Action {
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