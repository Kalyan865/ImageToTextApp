package com.example.imagetotextapp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE = 123

    private lateinit var txt: TextView
    private lateinit var img: ImageView
    private lateinit var translateBtn: Button

    //For using Camera
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                val bitMap = result.data?.extras?.get("data") as Bitmap
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
            txt.text = "Translation completed abcdefghijklmnopqrstuvwxyz."
        }
    }
}
