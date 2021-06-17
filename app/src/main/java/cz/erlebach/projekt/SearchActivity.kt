package cz.erlebach.projekt

import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SearchActivity : AppCompatActivity(), IScanner {

    private var bt: BLEscanner? = null

    private var textView: TextView? = null
    private var refresh = false // flag
    private val table = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val btManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bt = BLEscanner(this, btManager)


        textView = findViewById<TextView>(R.id.searchList)
        textView!!.movementMethod = ScrollingMovementMethod.getInstance()

        val btn = findViewById<Button>(R.id.searchSave)
        btn.setOnClickListener {
            val macAddress = getAddress()
            val intent = Intent()
            intent.putExtra(C.SEARCH_ACTIVITY_EXTRA_KEY, macAddress)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        findViewById<Button>(R.id.searchCancel).setOnClickListener {
            val intent = Intent()
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }

        val button = findViewById<Button>(R.id.SearchScan)
        button.setOnClickListener {

            if(!refresh) {
                refresh = true
                startScanning()
                button.text = resources.getString(R.string.buttonStop)

            } else {
                refresh = false
                stopScanning()
                button.text = resources.getString(R.string.buttonStart)
                button.backgroundTintList = this.resources.getColorStateList(R.color.my_gray, this.theme)

            }


        }
    }



    override fun ScannerCallback(result: ScanResult) {

        val rssi = result.rssi.toString()
        val deviceName = result.device.name
        val myMAC = result.device.address.toString()
        log("$myMAC rssi: $rssi")
        if (refresh) {
            if(deviceName != null) {
                table[myMAC] = rssi + " (" + deviceName + ")"
            } else {
                table[myMAC] = rssi
            }
            refreshTable()

        }
    }


    fun refreshTable() {

        var textBuffer = ""
        for ((k, v) in table) {
            textBuffer += "$k | $v \n"
        }
        textView?.text = textBuffer

    }
    private fun getAddress(): ArrayList<String> {

        val macAddress = arrayListOf<String>()

        for ((key, v) in table) {
            macAddress.add(key)
            log("$key: $v" )
        }
        return macAddress

    }



    private fun startScanning() {
        bt?.startDiscovery()
    }

    private fun stopScanning() {
        bt?.stopScanning()
    }
    private fun log(text: String) {
        Log.v("Search", text)
    }



}