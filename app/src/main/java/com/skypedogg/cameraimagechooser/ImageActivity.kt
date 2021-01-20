package com.skypedogg.cameraimagechooser

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.skypedogg.cameraimagechooser.databinding.ActivityImageBinding
import java.io.File

class ImageActivity : AppCompatActivity() {

    private lateinit var imageUtils: ImageUtils
    var imageFile: File? = null
    var imagePath: String? = null
    lateinit var binding: ActivityImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUtils = ImageUtils()
        setListeners()
        verifyIntent()


    }

    fun verifyIntent() {
        if (!intent.hasExtra("photo")) {
            val pickIntent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(pickIntent, GALLERY_INTENT)
        } else {
            imagePath = intent.extras?.get("photo") as String
            binding.imageView.setImageURI(Uri.fromFile(File(imagePath)))
            imagePath?.let { galleryAddPic(it) }
        }
    }

    fun setListeners() {
        binding.proceedScaling.setOnClickListener {
            val width = binding.width.text.toString().toInt()
            val height = binding.height.text.toString().toInt()
            imagePath?.let {
                val newPath = imageUtils.scaleImage(it, width, height, ImageUtils.ScaleType.CROP, getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.absolutePath)
                binding.imageView.setImageURI(Uri.fromFile(File(newPath)))
                galleryAddPic(newPath)
                imagePath = newPath
            }
        }
    }

    private fun galleryAddPic(currentPhotoPath: String) {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_INTENT) {
            if (resultCode == RESULT_OK) {
                binding.imageView.setImageURI(data?.data)
                imagePath = imageUtils.getPathFromUri(data?.data)
                imageFile = imageUtils.getFileFromUri(data?.data)

                galleryAddPic(imagePath!!)
            } else {
                val i = Intent(this@ImageActivity, MainActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
            }

        }
    }

    companion object {
        private const val GALLERY_INTENT = 456
    }
}