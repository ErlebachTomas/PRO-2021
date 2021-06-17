package cz.erlebach.projekt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.io.File

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        listFiles()

        val btn = findViewById<Button>(R.id.back)
        btn.setOnClickListener {

            finish()

        }

    }
    /** vypíše soubory ve složce */
    private fun listFiles() {


        val textView = findViewById<TextView>(R.id.searchList)
        textView!!.movementMethod = ScrollingMovementMethod.getInstance()


        val directory = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val path: String = directory.toString()
        Log.d("Files", "Path: $path")

        var textBuffer = ""


        val files: Array<File> = directory?.listFiles()!!
        Log.d("Files", "Size: " + files.size)
        textBuffer  +=  "Počet souborů: " + files.size.toString() + "\n"
        textBuffer  += "Soubory: \n"
        if(files.size > 0) {
            for (i in files.indices) {
                Log.d("Files", "FileName:" + files[i].name)
                textBuffer  +=  files[i].name + "\n"
            }
        } else {
            textBuffer  += "Žádné soubory nejsou k dispozici"
        }

        textView.text = textBuffer


    }

    private fun alertView(message: String, title: String = "Informace") {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(title)
            .setIcon(R.drawable.ic_launcher_foreground)
            .setMessage(message)
            .setPositiveButton("Ok") { _, _ -> showToast("ok") }.show()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}