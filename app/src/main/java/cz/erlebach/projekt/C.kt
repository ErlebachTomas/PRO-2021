package cz.erlebach.projekt


/**
 * List konstant
 */
object C {


    /**
     * Výchozí hodnota pro útlum prostředí
     */
    const val DEFAULT_N_VALUE: Float = 2.0F
    const val DEFAULT_RSSI_VAL : Float = -60.0F


    //2. aktivita
    const val SEARCH_ACTIVITY_REQUEST_CODE:Int = 2
    const val SEARCH_ACTIVITY_EXTRA_KEY: String = "SEARCH"

    //3. aktivita
    const val CALIBRATION_REQUEST_CODE: Int = 3
    const val CALIBRATION_N_KEY: String = "N_KEY"
    const val CALIBRATION_DEFAULT_RSSI_VAL_KEY: String = "RSSI_VAL_KEY"
    const val MAC_FILTER_KEY: String = "MAC_FILTER_KEY"

    // vychozi vzdalenost pro kalibraci
    const val CALIBRATION_d0 = 1.0
    const val CALIBRATION_d = 2.0

    //shared-preferences
    const val MAC: String = "MAC"
    const val Coefficient: String = "Coefficient_n"


}