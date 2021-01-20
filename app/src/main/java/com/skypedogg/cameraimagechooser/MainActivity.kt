package com.skypedogg.cameraimagechooser

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.skypedogg.cameraimagechooser.databinding.ActivityMainBinding

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService

class MainActivity : AppCompatActivity() {

    private lateinit var filePath: String
    private lateinit var binding: ActivityMainBinding
    private var extras: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.pickFromGallery.isEnabled = false
        binding.cameraCaptureButton.isEnabled = false

        if (hasPermissions()) {
            binding.pickFromGallery.isEnabled = true
            binding.cameraCaptureButton.isEnabled = true
        } else {
            askPermissions()
        }

        binding.pickFromGallery.setOnClickListener {
            val i = Intent(this@MainActivity, ImageActivity::class.java)
            startActivity(i)
        }
        binding.cameraCaptureButton.setOnClickListener {
            startCameraIntent()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault())
            .format(System.currentTimeMillis()) + ".bmp"
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "Photo_${timeStamp}_",
            ".jpg",
            dir
        ).apply {
            filePath = absolutePath
        }
    }


    private fun startCameraIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->

            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, PHOTO_TAKEN_CODE)
                }
            }
        }
    }


    private fun askPermissions() {
        ActivityCompat.requestPermissions(
            this, REQUIRED_PERMISSIONS, PERMISSIONS_CODE
        )
    }

    private fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_CODE) {
            if (hasPermissions()) {
                binding.pickFromGallery.isEnabled = true
                binding.cameraCaptureButton.isEnabled = true
            } else {
                Toast.makeText(this, "Permissions needed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHOTO_TAKEN_CODE) {
            if (resultCode == RESULT_OK) {
                val imageActivity = Intent(this@MainActivity, ImageActivity::class.java)
                imageActivity.putExtra("photo", filePath)
                startActivity(imageActivity)
            }
        }
    }


    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        private const val PERMISSIONS_CODE = 111
        private const val PHOTO_TAKEN_CODE = 112
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss"
    }
}