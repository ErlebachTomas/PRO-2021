package cz.erlebach.projekt

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class Logger(val value: Int, val instant: Instant, val mark: Int) {

    fun getValue():String{

        return value.toString()
    }

    fun getTimeStamp():String {
        // "HH:mm:ss.SSSSSSSSS"
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSSS").withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }
    fun getDateTime():String {

        val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
        return formatter.format(instant)

    }


}