package cz.erlebach.projekt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.os.AsyncTask
import java.util.ArrayList

/** Bluetooth Low Energy scanner */
class BLEscanner( callback: IScanner, btManager : BluetoothManager) {

    private val btAdapter: BluetoothAdapter = btManager.adapter
    private val btScanner: BluetoothLeScanner = btAdapter.bluetoothLeScanner

    private var scanSettings: ScanSettings = ScanSettings.Builder()
         .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
         .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
         .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
         .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
         .setReportDelay(0L)
         .build()

    /** Zahájí skenování okolí */
    fun startDiscovery() {

        AsyncTask.execute {
            btScanner.startScan(scanCallback)
        }
    }
    /** Zahájí skenování s MAC filtrem */
    fun startScanning(MAC: String) {

        val filters: ArrayList<ScanFilter> = ArrayList()
        val filter = ScanFilter.Builder()
                .setDeviceAddress(MAC)
                .build()

        filters.add(filter)
        startFilterScan(filters)
    }
    /** Zahájí skenování s MAC filtry */
    fun startScanning(MACaddresses: Array<String>) {

        val filters: ArrayList<ScanFilter> = ArrayList()
        for (address in MACaddresses)
        {
            val filter = ScanFilter.Builder()
                    .setDeviceAddress(address)
                    .build()
            filters.add(filter)
        }
        startFilterScan(filters)
    }
    /** zahájí skenování s nastaveným filtrem */
    private fun startFilterScan(filters: ArrayList<ScanFilter>) {

        AsyncTask.execute {
            btScanner.startScan(filters, scanSettings, scanCallback)
        }
    }
    /** Zastaví skenování */
    fun stopScanning() {

        println("stop BLE ")
        AsyncTask.execute { btScanner.stopScan(scanCallback) }

    }
    /** vrátí jméno adaptéru */
    fun getDeviceName():String {

        return btAdapter.name
    }


    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
           callback.ScannerCallback(result)
        }
    }


}