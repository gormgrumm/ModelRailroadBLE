package com.ModelRailroadBLE.main

import android.bluetooth.le.ScanResult
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import kotlinx.android.synthetic.main.row_scan_result.view.*
import org.jetbrains.anko.layoutInflater
//import kotlinx.android.synthetic.main.row_scan_result.view.device_name
//import kotlinx.android.synthetic.main.row_scan_result.view.mac_address
//import kotlinx.android.synthetic.main.row_scan_result.view.signal_strength

class ScanResultAdapter(
        private val items: List<ScanResult>,
        private val onClickListener: ((device: ScanResult, button: ImageView) -> Unit)
) : RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.context.layoutInflater.inflate(
                R.layout.row_scan_result,
                parent,
                false
        )
        //val imageView = view.freiButton
        return ViewHolder(view, onClickListener)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    class ViewHolder(
            private val view: View,
            //private val button: ImageView,
            private val onClickListener: ((device: ScanResult, button: ImageView) -> Unit)
    ) : RecyclerView.ViewHolder(view) {

        fun bind(result: ScanResult) {
            //view.device_name.text = result.device.name ?: "Unnamed"
            //view.mac_address.text = result.device.address
            //view.signal_strength.text = "${result.rssi} dBm"
            //view.freiButton.setOnClickListener { onClickListener.invoke(result) }
            Log.i("Flow","Adding device in ViewHolder ${result.device}")
            view.freiButton.setOnClickListener{ onClickListener.invoke(result,view.freiButton) }
            view.langsamButton.setOnClickListener { onClickListener.invoke(result,view.langsamButton) }
            view.haltButton.setOnClickListener { onClickListener.invoke(result,view.haltButton) }
            view.notButton.setOnClickListener { onClickListener.invoke(result,view.notButton) }

        }
    }
}

