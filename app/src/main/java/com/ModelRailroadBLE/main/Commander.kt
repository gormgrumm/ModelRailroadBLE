package com.ModelRailroadBLE.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.send_commands.*
import java.util.*

class Commander : AppCompatActivity() {

    // This does not do anything - playing with launching another activity.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.send_commands)
        haltbutton.setOnClickListener {
            val testVal = "l"
            val bytes = testVal.toByteArray()
            val serviceForWrite = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            val characteristicForWrite = UUID.fromString("00002102-0000-1000-8000-00805f9b34fb")
        //    val ch = bluetoothGatt.getService(serviceForWrite).getCharacteristic(characteristicForWrite)
        //    writeCharacteristic(ch, bytes)
        }
        Log.w("status", "changed to Commander activity")

    }

}

