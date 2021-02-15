package com.utveckla.xdays

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.utveckla.xdays.db.AppDatabase
import com.utveckla.xdays.db.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var job: Job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main)
    private val appFolderName = "xDays"

    companion object {
        const val REQUEST_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_export_events -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION)
                } else {
                    exportEvents()
                }
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportEvents()
            }
        }
    }

    private fun exportEvents() {
        val dir = applicationContext.getExternalFilesDir(null)?.absolutePath.toString()

        File(dir).mkdirs()

        val file = "%1\$tY%1\$tm%1\$td%1\$tH%1\$tM%1\$tS.csv".format(Date())

        scope.launch {
            applicationContext.let { it ->
                val events = AppDatabase(it).eventDao().getAllEvents()

                File("$dir/$file").printWriter(Charsets.UTF_8).use {
                    val sdf = SimpleDateFormat("yyyy/MM/dd")

                    it.println("id,name,date")
                    for (event: Event in events) {
                        it.println("" + event.id + "," + event.name + "," + sdf.parse(event.date))
                    }
                }

                saveInDownloads(applicationContext, File("$dir/$file"))
                saveComplete()
            }
        }
    }

    private fun saveComplete() {
        AlertDialog.Builder(this).apply {
            setTitle("Events saved")
            setMessage("Your events have been exported")
            setPositiveButton("Sweet!") { _, _ ->
            }
        }.create().show()
    }

    private fun saveInDownloads(appContext: Context, fromFile: File) {
        val destination = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveForQ(appContext, fromFile.name)
        } else {
            saveForBelowQ(fromFile.name)
        }

        destination?.let {
            try {
                val source = FileInputStream(fromFile)
                destination.channel.transferFrom(source.channel, 0, source.channel.size())
                source.close()
                destination.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveForQ(appContext: Context, fileName: String): FileOutputStream? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.CONTENT_TYPE, "text/csv")
            put(MediaStore.Downloads.DATE_ADDED, (System.currentTimeMillis() / 1000).toInt())
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + appFolderName)
        }

        val resolver = appContext.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        return uri?.let {
            resolver.openOutputStream(uri) as FileOutputStream
        }
    }

    private fun saveForBelowQ(fileName: String): FileOutputStream {
        val path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).toString()

        val directory = File(path, appFolderName)

        directory.mkdirs()

        val file = File(directory, fileName)
        return FileOutputStream(file)
    }
}