package com.example.imagetotextapp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private var DATA_PATH : String? = null
    private val TESSDATA = "tessdata"
    private val REQUEST_CODE = 123
    private var bitMap: Bitmap? = null

    private lateinit var txt: TextView
    private lateinit var img: ImageView
    private lateinit var translateBtn: Button

    //For using Camera
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DATA_PATH = applicationContext.filesDir.path + "/TesseractSample/"
        txt = findViewById(R.id.textValue)
        img = findViewById(R.id.capturedImg)
        translateBtn = findViewById(R.id.translate_btn)

        //Check Camara permissions provided. If not provided request for permissions
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                listOf(android.Manifest.permission.CAMERA).toTypedArray(),
                REQUEST_CODE
            )
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == RESULT_OK) {
                bitMap = result.data?.extras?.get("data") as Bitmap
                //ste image in image view
                translateBtn.visibility = View.VISIBLE
                img.setImageBitmap(bitMap)
            }
        }

        //Capture image
        findViewById<Button>(R.id.cameraBtn).setOnClickListener{
            img.visibility = View.VISIBLE
            txt.visibility = View.GONE
            val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (pictureIntent.resolveActivity(packageManager) != null) {
                cameraLauncher.launch(pictureIntent)
            }
        }

        //Textify
        translateBtn.setOnClickListener{
            img.visibility = View.GONE
            txt.visibility = View.VISIBLE
            prepareTesseract()
            if (bitMap == null) {
                txt.text = "Image not found"
            }
            bitMap?.let {
                val textVal = extractText(it)
                txt.text = textVal
            }
        }
    }

    private fun prepareTesseract() {
        try {
            prepareDirectory(DATA_PATH + TESSDATA)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        copyTessDataFiles(TESSDATA)
    }

    /**
     * Prepare directory on external storage
     *
     * @param path
     * @throws Exception
     */
    private fun prepareDirectory(path: String) {
        val dir = File(path)
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e("ERROR: Creation of directory",  "$path failed, check does Android Manifest have permission to write to external storage.")
            }
        } else {
            Log.i( "Created directory",  " $path")
        }
    }

    /**
     * Copy tessdata files (located on assets/tessdata) to destination directory
     *
     * @param path - name of directory with .traineddata files
     */
    private fun copyTessDataFiles(path: String) {
        try {
            val fileList = assets.list(path)
            for (fileName in fileList!!) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                val pathToDataFile: String = DATA_PATH + path + "/" + fileName
                if (!File(pathToDataFile).exists()) {
                    val `in` = assets.open("$path/$fileName")
                    val out: OutputStream = FileOutputStream(pathToDataFile)

                    // Transfer bytes from in to out
                    val buf = ByteArray(1024)
                    var len: Int
                    while (`in`.read(buf).also { len = it } > 0) {
                        out.write(buf, 0, len)
                    }
                    `in`.close()
                    out.close()
                    Log.d(TAG, "Copied " + fileName + "to tessdata")
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Unable to copy files to tessdata $e")
        }
    }

    private fun extractText(bitmap: Bitmap): String {
        val tessBaseApi = TessBaseAPI()
        tessBaseApi.init(DATA_PATH, "eng")

//       //EXTRA SETTINGS
//        //For example if we only want to detect numbers
//        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890");
//
//        //blackList Example
//        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
//                "YTRWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");
        Log.d(TAG, "Training file loaded")
        tessBaseApi.setImage(bitmap)
        var extractedText = "empty result"
        try {
            extractedText = tessBaseApi.utF8Text
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Error in recognizing text.")
        }
        tessBaseApi.end()
        return extractedText
    }
}
