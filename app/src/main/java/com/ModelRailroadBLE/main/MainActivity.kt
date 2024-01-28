package com.ModelRailroadBLE.main



import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.row_command.view.*
import kotlinx.android.synthetic.main.row_scan_result.*
import org.jetbrains.anko.alert
import java.util.*


private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2
private const val GATT_MAX_MTU_SIZE = 517
private const val serviceUUID = "00001101-0000-1000-8000-00805f9b34fb"

class MainActivity : AppCompatActivity() {

    // Defining global BluetoothGatt variable to hold the Gatt connection.
    private lateinit var bluetoothGatt: BluetoothGatt

    // Defining global ImageView variable to hold ID of button pressed.
    // After connect, the aspect/payload will be sent to the device based on this variable
    private lateinit var btn : ImageView

    /*******************************************
     * Properties
     *******************************************/
    // callback for changes to the gatt connection
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // if device is now connected, discover the service(s)
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    bluetoothGatt = gatt
                    gatt.device.address
                    Handler(Looper.getMainLooper()).post {
                        bluetoothGatt?.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                gatt.close()
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            // when services are discovered request change to MTU
            with(gatt) {
                Log.w("BluetoothGattCallback", "Discovered ${services.size} services for ${device.address}")
                printGattTable() // See implementation just above this section
                //gatt.requestMtu(GATT_MAX_MTU_SIZE)
                // Consider connection setup as complete here

                if (isScanning) {
                    stopBleScan()
                }

                Log.i("Flow","hits callback write")
                var payloadString = ""
                if (btn.id == 2131230894){
                    payloadString = "f"
                }
                if (btn.id == 2131230928){
                    payloadString = "l"
                }
                if (btn.id == 2131230905){
                    payloadString = "h"
                }
                if (btn.id == 2131230999){
                    payloadString = "n"
                }
                Log.i("Payload callback", "device: ${gatt.device}, payload: $payloadString")
                writeToDevice(bluetoothGatt.device, bluetoothGatt, payloadString)

                //}
                //val testVal = "l"
                //val bytes = testVal.toByteArray()
                //val serviceForWrite = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
                //val characteristicForWrite = UUID.fromString("00002102-0000-1000-8000-00805f9b34fb")
                //val ch = bluetoothGatt.getService(serviceForWrite).getCharacteristic(characteristicForWrite)
                //writeCharacteristic(ch, bytes)
                //Log.i("Payload callback", "n")
                //writeToDevice(gatt.device, bluetoothGatt, "n")

                // original intent for starting up next screen...
                // val myIntent = Intent(this@MainActivity, Commander::class.java)
                // startActivityForResult(myIntent, 0)


                // Call site

            }
        }
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.w("BluetoothGattCallback", "ATT MTU changed to $mtu, success: ${status == BluetoothGatt.GATT_SUCCESS}")

            // previously, write was only done after MTU change. Now it works anyway... hence commented out, write is moved to after services are discovered.
            //*
//            var payloadString = ""
//            if (btn.id == 2131230894){
//                payloadString = "f"
//            }
//            if (btn.id == 2131230928){
//                payloadString = "l"
//            }
//            if (btn.id == 2131230905){
//                payloadString = "h"
//            }
//            if (btn.id == 2131230999){
//                payloadString = "n"
//            }
//            writeToDevice(bluetoothGatt.device, bluetoothGatt, "$payloadString")

            // OLD DIRECT TESTING OF WRITE
            //val testVal = "f"
            //val bytes = testVal.toByteArray()
            //val serviceForWrite = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            //val characteristicForWrite = UUID.fromString("00002102-0000-1000-8000-00805f9b34fb")
            //val ch = bluetoothGatt.getService(serviceForWrite).getCharacteristic(characteristicForWrite)
            //writeCharacteristic(ch, bytes)
        }

        // write to device
        fun writeToDevice(device: BluetoothDevice, gt: BluetoothGatt , payloadString: String){

            val bytes = payloadString.toByteArray()
            val serviceForWrite = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            val characteristicForWrite = UUID.fromString("00002102-0000-1000-8000-00805f9b34fb")
            val ch = bluetoothGatt.getService(serviceForWrite).getCharacteristic(characteristicForWrite)
            Log.i("Gatt", "$bluetoothGatt")
            writeCharacteristic(ch, bytes)
            //bluetoothGatt.disconnect()
            bluetoothGatt.close()
            bluetoothGatt.disconnect()
            //Log.i("Connections", "Number of open connections: $")
        }

    }

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> error("Characteristic ${characteristic.uuid} cannot be written to")
        }

        bluetoothGatt?.let { gatt ->
            characteristic.writeType = writeType
            characteristic.value = payload
            gatt.writeCharacteristic(characteristic)
            Log.i("writeCharacteristic", "${gatt.device} : $payload")
        } ?: error("Not connected to a BLE device!")
    }


    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    // array of scanfilters
    private val scanFilters = arrayListOf<ScanFilter>()

    // create scanfilter
    private val scanFilter = ScanFilter.Builder().setServiceUuid(
            ParcelUuid.fromString(serviceUUID)
    ).build()

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }
    private val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread { scan_button.text = if (value) "Stop Scan" else "Start Scan" }
        }

    private val scanResults = mutableListOf<ScanResult>()

    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) { result, button ->
            // User tapped on a scan result.
            Log.i("Tap", "Clicked on ${result.device}, button: ${button.id}, ${button}")
            btn = button
            if (isScanning) {
                stopBleScan()
            }
            // set the payload string according to the button pressed
            var payloadString = ""
            if (button.id == 2131230894){
                payloadString = "f"
            }
            if (button.id == 2131230928){
                payloadString = "l"
            }
            if (button.id == 2131230905){
                payloadString = "h"
            }
            if (button.id == 2131230999){
                payloadString = "n"
            }
            Log.i("Tap", "Payload: $payloadString")
            // connect to device tapped - the scan result device
            with(result.device) {
                Log.i("ScanResultAdapter", "Connecting to $address")
                connectGatt(applicationContext, false, gattCallback)


            }
        }
    }

    val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    /*******************************************
     * Activity function overrides
     *******************************************/

    // setup the activity, set button listener and list of devices
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scan_button.setOnClickListener {
            if (isScanning) {
                stopBleScan()
            } else {
                startBleScan()
            }
        }
        setupRecyclerView()
    }

    // function to print the services available when connected
    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first?")
            return
        }
        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(
                    separator = "\n|--",
                    prefix = "|--"
            ) { it.uuid.toString() }
            Log.i("printGattTable", "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable"
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }

    private fun startBleScan() {
        Log.i("Starts", "start ble")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            scanResults.clear()
            scanResultAdapter.notifyDataSetChanged()

            // add the scanfilter to scanfilters...
            scanFilters.add(scanFilter)
            bleScanner.startScan(scanFilters, scanSettings, scanCallback)
            isScanning = true
        }
    }
    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }
        runOnUiThread {
            alert {
                title = "Location permission required"
                message = "Starting from Android M (6.0), the system requires apps to be granted " +
                        "location access in order to scan for BLE devices. The app does not use" +
                        "the permission for anything else."

                positiveButton(android.R.string.ok) {
                    requestPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }.show()
        }
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

    private fun setupRecyclerView() {
        scan_results_recycler_view.apply {
            adapter = scanResultAdapter
            layoutManager = LinearLayoutManager(
                    this@MainActivity,
                    RecyclerView.VERTICAL,
                    false
            )
            isNestedScrollingEnabled = false
        }

        val animator = scan_results_recycler_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    startBleScan()
                }
            }
        }
    }



    /*******************************************
     * Callback bodies
     *******************************************/

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                scanResultAdapter.notifyItemChanged(indexQuery)
            //} else if (result.device.name == "SignalController"){
            } else if (true){
                with(result.device) {
                    //Log.i(“ScanCallback”, "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                    //Log.i("ScanCallback", "Found device, name: ${name ?: "Unnamed"}, address: $address")
                    //Log.w("ScanCallback", "Connecting to $address")
                    //connectGatt(applicationContext, false, gattCallback)
                }
                scanResults.add(result)
                scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }
        override fun onScanFailed(errorCode: Int) {
            Log.e("Scancallback", "onScanFailed: code $errorCode")
        }
    }

    /*******************************************
     * Extension functions
     *******************************************/

    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun BluetoothGattCharacteristic.isReadable(): Boolean =
            containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    fun BluetoothGattCharacteristic.isWritable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
            containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }
}

