package com.skypedogg.cameraimagechooser

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.skypedogg.cameraimagechooser.databinding.ActivityCameraBinding
import com.skypedogg.cameraimagechooser.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null

    private lateinit var output: File
    private lateinit var outputDir: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    private fun takePhoto(): Pair<File?, String?> {

        val imageCapture = imageCapture ?: return Pair(null, null)

        var photoPath: String? = null
        val photoFile = File(
                outputDir,
                SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault())
                        .format(System.currentTimeMillis()) + ".bmp"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        photoPath = outputFileResults.savedUri?.path
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e(callingActivity!!.shortClassName, exception.localizedMessage!!)
                    }

                }
        )
        return Pair(photoFile, photoPath)
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss"
    }
}