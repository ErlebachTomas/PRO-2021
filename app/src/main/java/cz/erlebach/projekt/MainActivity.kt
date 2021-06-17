package cz.erlebach.projekt

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle



class MainActivity : AppCompatActivity(), IScanner {

    private val requestCode:Int = 1

    var btScanner: BLEscanner? = null

    // Bluetooth Screen table
    var textView: TextView? = null
    var refresh = false // flag

    var dateTimeTex: TextView? = null

    var macAddress = arrayListOf<String>()

    var MACFilter: String = ""

    var signal : Signal = Signal(C.DEFAULT_RSSI_VAL,C.DEFAULT_N_VALUE)


    var markCounter: Int = 0 //značovač

    private var coeficientN : Float = C.DEFAULT_N_VALUE
    private var defaultRSSIVal :Float = C.DEFAULT_RSSI_VAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       loadSharedPreferences()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // zruší zamykání displeje

        btScanner = BLEscanner(this, getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)

        textView = findViewById<TextView>(R.id.signalVal)
        textView!!.movementMethod = ScrollingMovementMethod.getInstance()

        dateTimeTex = findViewById<TextView>(R.id.dateTime)

        val myName = findViewById<TextView>(R.id.device)
        myName.text = btScanner?.getDeviceName()


        loadSpinners()

        checkpermissions()

        val button = findViewById<Button>(R.id.start)
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

        val btnWrite = findViewById<Button>(R.id.write)
        btnWrite.setOnClickListener {

            val mySpinner = findViewById<View>(R.id.profile_spinner) as Spinner
            val spinnerVal = mySpinner.selectedItem.toString()


            button.backgroundTintList = this.resources.getColorStateList(R.color.purple_500,this.theme)
            if (signal.getSize() > 0) {
                val w = WriterCSV()
                w.write(this, signal, spinnerVal)

                w.exportProfile(this, spinnerVal, getCurrentTime(),
                        signal.getMin().toString(),
                        signal.getMean().toString(),
                        signal.getMax().toString()
                )

                showToast("zapsano do souboru $spinnerVal")

                markCounter = 0

            } else {
                showToast("nemám co zapsat")
            }

        }

        val btnSearch = findViewById<View>(R.id.floatingActionButton) as FloatingActionButton
        btnSearch.setOnClickListener{
            selectMAC()
        }

        findViewById<View>(R.id.infoButton).setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        val btnMarker = findViewById<Button>(R.id.mark)
        btnMarker.setOnClickListener {

            markCounter += 1
            btnMarker.text = "Zapsat $markCounter"

        }

        findViewById<View>(R.id.Calibration).setOnClickListener {
            val intent = Intent(this, CalibrationActivity::class.java)
            intent.putExtra(C.MAC_FILTER_KEY,MACFilter)
            startActivityForResult(intent, C.CALIBRATION_REQUEST_CODE)
        }

        refreshInfo()

    }

    private fun loadSharedPreferences() {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        val macFromSharedPref = sharedPref.getString(C.MAC, null)
        if (macFromSharedPref == null) {
            selectMAC()
        } else {
            MACFilter = macFromSharedPref
            macAddress.add(macFromSharedPref)
        }
        coeficientN = sharedPref.getFloat(C.Coefficient, C.DEFAULT_N_VALUE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /* Návratová funkce z aktivit */
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            C.SEARCH_ACTIVITY_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                val acceptedData = data?.getStringArrayListExtra(C.SEARCH_ACTIVITY_EXTRA_KEY)
                if (acceptedData  != null) {
                    macAddress.clear()
                    macAddress.addAll(acceptedData)

                    loadSpinners()
                } else {
                   showToast("Err: Zadne MAC")
                }
            }
            C.CALIBRATION_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                coeficientN = data!!.getFloatExtra(C.CALIBRATION_N_KEY,C.DEFAULT_N_VALUE)
                defaultRSSIVal = data.getFloatExtra(C.CALIBRATION_DEFAULT_RSSI_VAL_KEY,C.DEFAULT_RSSI_VAL)
                refreshInfo()
            } else {
                coeficientN = C.DEFAULT_N_VALUE
                defaultRSSIVal = C.DEFAULT_RSSI_VAL

            }

        }

    }

    private fun selectMAC() {
        val intent = Intent(this, SearchActivity::class.java)
        startActivityForResult(intent, C.SEARCH_ACTIVITY_REQUEST_CODE)
    }

    private fun loadSpinners() {

        val sp = this.getPreferences(Context.MODE_PRIVATE)

        val dropdownMAC = findViewById<Spinner>(R.id.MAC_spinner)

        if (macAddress.isNotEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,  macAddress)
            dropdownMAC.adapter = adapter
            dropdownMAC.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                    val item = parent.getItemAtPosition(pos)
                    MACFilter = item.toString()
                    sp.edit().putString(C.MAC,MACFilter).apply()
                    log(MACFilter)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        val dropdownProfile = findViewById<Spinner>(R.id.profile_spinner)
        val adapterProfile = ArrayAdapter.createFromResource(this,R.array.Profiles, android.R.layout.simple_spinner_dropdown_item)
        dropdownProfile.adapter = adapterProfile

    }

    fun checkpermissions() {
        log("kontrola oprávnění ")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            log("vyzadovano udeleni LOCATION opravneni ")
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    requestCode)
        } else {
            log("opravneni udeleno")
        }
    }

    private fun log(text: String) {
        Log.v("Main", text)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun ScannerCallback(result: ScanResult) {
    // btScanner scan callback

        val rssi = result.rssi.toString()
        val myMAC = result.device.address.toString()

        if (myMAC == MACFilter) {
            signal.add(result.rssi, Instant.now(), markCounter)
        }

        log(myMAC + " rssi: " + rssi)


        textView?.text = rssi
        dateTimeTex?.text = "id: $markCounter"

        if (!signal.isEmpty()) {
            val speed = signal.getCurrentVelocity(signal.getSize()-1,true) * 3.6

            findViewById<TextView>(R.id.speed).text = String.format("%.2f", speed) + " km/h"
        } else {
            findViewById<TextView>(R.id.speed).text = ""
        }
    }

    private fun startScanning() {
        log("start")
        showToast("start")
        signal.clear()  // reset loggu pro nove mereni
        markCounter = 0
        btScanner?.startScanning(MACFilter)
    }

    private fun stopScanning() {
        log("stop")
        showToast("stop")

        btScanner?.stopScanning()

        if (signal.getSize() > 0) {
            refreshStats()
        } else {
            findViewById<TextView>(R.id.stats).text = this.getString(R.string.NaN)
        }

    }
    /** Zobrazí statistiky profilu */
    private fun refreshStats() {

        val max = signal.getMax()
        val min = signal.getMin()
        val mean = signal.getMean()

        val myName = findViewById<TextView>(R.id.stats)
        myName.text = "Min: $min, Průměr: $mean, Max: $max"



    }
    private fun refreshInfo() {
        findViewById<TextView>(R.id.settings).text = "n: $coeficientN \n val: $defaultRSSIVal"

    }

    private fun getCurrentTime(): String {
        val instant: Instant = Instant.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val zoneId: ZoneId = ZoneId.systemDefault()
        val zdt: ZonedDateTime = ZonedDateTime.ofInstant(instant, zoneId)
        return zdt.format(formatter.withLocale(resources.configuration.locales.get(0)))
    }




}


