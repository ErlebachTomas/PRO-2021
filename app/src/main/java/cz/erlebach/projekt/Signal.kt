package cz.erlebach.projekt

import java.time.Duration
import java.time.Instant
import java.util.ArrayList
import kotlin.math.pow

class Signal(_RSSId0: Float, _coeficientN: Float) {

    var RSSId0 = _RSSId0
    var coeficientN = _coeficientN

   val list: ArrayList<Logger> = ArrayList()

   private var sum: Int = 0
   private var max: Int = 0
   private var min: Int = 0

    fun add(value: Int, instant: Instant, mark: Int) {

        if (list.isEmpty()) {
            max = value
            min = value
            sum = value

        } else {
            sum += value
            if ( value > max) {
                max = value
            }
            if ( value < min) {
                min = value
            }
        }


        list.add(Logger(value, instant, mark ))

    }


    fun getSmoothedVal(index: Int) : Double {

        return if(index == 0) {
            list[0].value.toDouble()
        } else {
            list[index].value * 0.1 + 0.9 * list[index-1].value
        }

    }
    fun getVal(index: Int): Int {
        return list[index].value
    }

    fun getLog(index: Int): Logger {
        return list[index]
    }
    fun getInstant(index: Int): Instant {
        return list[index].instant
    }

    fun getDuration(a : Int, b : Int): Duration {

        return Duration.between(list[a].instant, list[b].instant)

    }

    fun getMillisBetween(a : Int, b : Int): Long {
        return this.getDuration(a,b).toMillis()
    }
    fun getTPlusString(a : Int): String {

        return this.getDuration(0,a).toMillis().toString()
    }

    private fun getVelocity(a : Int, b : Int, smooth: Boolean = false ): Double {
        val duration = Duration.between(list[a].instant, list[b].instant).toMillis()
        return if (smooth) {
            (calculateDistance(getSmoothedVal(a)) - calculateDistance(getSmoothedVal(b)) ) / (duration * 0.001)
        } else {
            (calculateDistance(list[a].value) - calculateDistance(list[b].value) ) / (duration * 0.001)
        }
    }

    fun getCurrentVelocity(i: Int, smooth: Boolean = false): Double {
        return if (i == 0) {
            0.0
        } else {
           this.getVelocity(i-1,i, smooth)
        }
    }

    fun getDistance(index : Int, smooth: Boolean = false): Double {

      return if (smooth) {
          calculateDistance(this.getSmoothedVal(index))
      } else {
           calculateDistance(list[index].value)
       }

    }

    fun calculateDistance(value : Double): Double {
        val exponent = (RSSId0 - value) / (10 * coeficientN)
        return (10.0).pow(exponent)
    }
    fun calculateDistance(value : Int): Double {
        return calculateDistance(value.toDouble())
    }

    fun clear() {
        max = 0
        min = 0
        list.clear()
    }

    fun isEmpty() : Boolean {
        return list.isEmpty()
    }


    fun getMean(): Float {
        return  sum / list.size.toFloat()
    }

    fun getMax(): Int {
        return max
    }
    fun getMin(): Int {
        return min
    }
    fun getSize() : Int {
        return list.size
    }
    fun getValString(i :Int):String {
        return list[i].value.toString()
    }
   fun getMarkString(i :Int):String {
       return list[i].mark.toString()
   }
    fun getTimeStamp(i :Int):String {
        return list[i].getTimeStamp()

    }
    fun getSmoothedValString(index: Int): String {
        return this.getSmoothedVal(index).toString()
    }
    fun getCurrentVelocityString(index: Int, smooth: Boolean = false): String {
        return this.getCurrentVelocity(index, smooth).toString()
    }
    fun getDistanceString(index: Int,smooth: Boolean = false ): String {
        return this.getDistance(index, smooth).toString()
    }

}