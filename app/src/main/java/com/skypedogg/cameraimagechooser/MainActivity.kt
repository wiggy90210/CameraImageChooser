package com.skypedogg.cameraimagechooser

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.skypedogg.cameraimagechooser.databinding.ActivityMainBinding

import java.io.File
import java.util.concurrent.ExecutorService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null

    private lateinit var output: File
    private lateinit var outputDir: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (hasPermissions()) {
            //startCamera()
        } else {
            askPermissions()
        }

        setListeners()
        outputDir = getOutputDirectory()
    }

    private fun getOutputDirectory(): File {
        val directory = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return when (directory != null && directory.exists()) {
            true -> directory
            else -> filesDir
        }
    }

    private fun setListeners() {
        binding.cameraCaptureButton.setOnClickListener { takePhoto() }
    }

    private fun askPermissions() {
        //ActivityCompat.requestPermissions(
        //    this, REQUIRED_PERMISSIONS, PERMISSIONS_CODE)
        //)
    }

    private fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )
            } catch (ex: Exception) {
                Log.e(callingActivity!!.shortClassName, "Use case binding failed", ex)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_CODE) {
            if (hasPermissions()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions needed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss"
        private const val PERMISSIONS_CODE = 111
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}