package com.skypedogg.cameraimagechooser

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.skypedogg.cameraimagechooser.databinding.ActivityCameraBinding
import com.skypedogg.cameraimagechooser.databinding.ActivityImageBinding
import java.io.File

class ImageActivity : AppCompatActivity() {

    private lateinit var imageUtils: ImageUtils
    var imageFile: File? = null
    var imagePath: String? = null
    lateinit var binding: ActivityImageBinding
    lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUtils = ImageUtils()

        if (!intent.hasExtra("photo")) {
            val pickIntent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(pickIntent, GALLERY_INTENT)
        } else {
            imagePath = intent.extras?.get("photo") as String
            binding.imageView.setImageURI(Uri.fromFile(File(imagePath)))
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_INTENT) {
            binding.imageView.setImageURI(data?.data)
            imagePath = imageUtils.getPathFromUri(data?.data)
            imageFile = imageUtils.getFileFromUri(data?.data)

        }
    }

    companion object {
        private const val GALLERY_INTENT = 456
    }
}