package cz.erlebach.projekt

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult

interface IScanner {

    /** Rozhraní pro manipulaci s UI */

     fun ScannerCallback(result: ScanResult)

}