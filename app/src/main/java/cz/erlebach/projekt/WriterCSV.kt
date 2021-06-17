package cz.erlebach.projekt


import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.Duration

class WriterCSV  {

    fun write(context: Context, s: Signal, profile: String) {

        if(s.isEmpty()) {
            throw IllegalArgumentException("Log list nesmi být prazdny")
        }

        val CSV_HEADER = "Time;Zone;T+;RSSI;Filter;Distance;Velocity;Smooth distance;Smooth velocity"
        val fileName = profile + " " + s.getLog(0).getDateTime() + ".csv"
        val destination = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        var fileWriter: FileWriter? = null


        try {
            fileWriter = FileWriter(destination)

            fileWriter.append(CSV_HEADER)
            fileWriter.append('\n')

            for(i in s.list.indices)  {

                fileWriter.append(s.getTimeStamp(i))
                fileWriter.append(';')

                fileWriter.append(s.getMarkString(i))
                fileWriter.append(';')

                fileWriter.append(s.getTPlusString(i))
                fileWriter.append(';')

                fileWriter.append(s.getValString(i))
                fileWriter.append(';')

                fileWriter.append(s.getSmoothedValString(i))
                fileWriter.append(';')

                fileWriter.append(s.getDistanceString(i))
                fileWriter.append(';')

                fileWriter.append(s.getCurrentVelocityString(i))
                fileWriter.append(';')

                fileWriter.append(s.getDistanceString(i, true))
                fileWriter.append(';')

                fileWriter.append(s.getCurrentVelocityString(i,  true))
                fileWriter.append(';')

                fileWriter.append('\n')
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fileWriter!!.flush()
                fileWriter.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    fun exportProfile(context: Context, Profile: String, datetime: String, min: String, mean: String, max: String ) {

        val CSV_HEADER = "dateTime;profile;mean;min;max"
        val fileName = "stats.csv"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        val onlyAppend = file.exists()

        var fileWriter: FileWriter? = null

        try {
            fileWriter = FileWriter(file,true)

            if(!onlyAppend) {
                fileWriter.append(CSV_HEADER)
                fileWriter.append('\n')
            }

            fileWriter.append(datetime)
            fileWriter.append(';')
            fileWriter.append(Profile)
            fileWriter.append(';')
            fileWriter.append(mean)
            fileWriter.append(';')
            fileWriter.append(min)
            fileWriter.append(';')
            fileWriter.append(max)
            fileWriter.append('\n')


        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fileWriter!!.flush()
                fileWriter.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

}