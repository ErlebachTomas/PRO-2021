package cz.erlebach.projekt


import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.ArrayList
import kotlin.math.abs
import kotlin.math.log10


class CalibrationActivity : AppCompatActivity(), IScanner {

    private val  LIMIT =  10

    private var scannerRSSIs = arrayListOf<Int>()
    private var stepList = arrayListOf<Float>()

    private var bt: BLEscanner? = null
    var n : Float = 0.0F
    var RSSId0 : Float = 0.0F
    private var stepCounter = 1
    private var stepOverall = 2

    private var refresh = false // flag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calibration)

        val btManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        bt = BLEscanner(this, btManager)

        val macFilter = intent.getStringExtra(C.MAC_FILTER_KEY)

        val button = findViewById<Button>(R.id.Cstart)
        button.setOnClickListener {

            if(!refresh) {
                refresh = true

                if (macFilter != null) {
                    scannerRSSIs.clear()
                    bt!!.startScanning(macFilter)
                } else {
                    val msg = this.getString(R.string.sharedPrefError)
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
                button.backgroundTintList = this.resources.getColorStateList(R.color.my_gray, this.theme)
                button.text = resources.getString(R.string.buttonStop)

            } else {
                refresh = false
                startButton()
                bt!!.stopScanning()
                showToast("Zrušeno")

            }

        }


        findViewById<View>(R.id.CAback).setOnClickListener {

            val intent = Intent()
            if (n  > 0) {
                intent.putExtra(C.CALIBRATION_N_KEY, n)
                intent.putExtra(C.CALIBRATION_DEFAULT_RSSI_VAL_KEY, RSSId0)
                setResult(Activity.RESULT_OK, intent)
            } else {
                setResult(Activity.RESULT_CANCELED, intent)
            }

            finish()

        }

    }

    override fun ScannerCallback(result: ScanResult) {
        val rssi = result.rssi.toString()
        val mac = result.device.address.toString()
        println("CalibrationActivity call")
        println("BLE $mac rssi: $rssi")

        scannerRSSIs.add(result.rssi)
        val size = scannerRSSIs.size
        findViewById<TextView>(R.id.steps).text = "rssi[$size]: $rssi \n"

        if(size >= LIMIT ) {
            bt!!.stopScanning()
            stepController()
            stepCounter += 1
        }

    }

    private fun startButton() {
        val button = findViewById<Button>(R.id.Cstart)
        button.text = resources.getString(R.string.buttonStart)
        button.backgroundTintList = this.resources.getColorStateList(R.color.purple_500,this.theme)
    }


    private fun stepController() {
        refresh = false


        val mean = mean(scannerRSSIs)
        stepList.add(mean)

        val textView = findViewById<TextView>(R.id.cInfo)

        textView.append("RSSI(d$stepCounter): $mean\n")

        if(stepCounter == stepOverall) {

           n = lostPath(stepList[0],C.CALIBRATION_d0,stepList[1],C.CALIBRATION_d).toFloat()
           RSSId0 =  stepList[0]
           textView.append("KALIBRACE DOKONČENA:\n")
           textView.append("n: $n\n")
           textView.append("RSSI_VAL: $RSSId0\n")

            findViewById<Button>(R.id.Cstart).isEnabled = false

        } else {
            startButton()
        }



    }

    private fun mean(list: ArrayList<Int>): Float {

        var mean = 0.0F
        var sum = 0
        for (element in list) {

            sum += element
            mean = sum / list.size.toFloat()
        }
        return mean

    }

    private fun lostPath(RSSId0: Float, d0: Double, RSSId: Float, d: Double): Double {

        return abs( (RSSId0 - RSSId) / (10 * log10(d / d0)) )

    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}