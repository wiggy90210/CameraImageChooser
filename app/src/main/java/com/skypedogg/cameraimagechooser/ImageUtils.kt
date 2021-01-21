package com.skypedogg.cameraimagechooser

import android.R.attr.bitmap
import android.graphics.*
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class ImageUtils {

    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss"

    fun getFileFromUri(uri: Uri?): File? {
        uri?.let {
            val path: String? = it.path
            if (path != null) {
                return File(path)
            }
            return null
        }
        return null
    }

    fun getPathFromUri(uri: Uri?): String? {
        uri?.let {
            return it.path
        }
        return null
    }

    fun getBitmapFromPath(absolutePath: String): Bitmap {
        return BitmapFactory.decodeFile(absolutePath)
    }

    fun getBitmapFromFile(file: File): Bitmap {
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    fun File.compressImage(size: Int): File {
        val outputStream = ByteArrayOutputStream()
        val originalSize = this.length()
        val bitmap = getBitmapFromFile(this)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
        this.writeBytes(outputStream.toByteArray())
        if (this.length() > originalSize)
            this.compressImage(size)
        return this
    }

    fun scaleImage(path: String, dstWidth: Int, dstHeight: Int, scaleType: ScaleType, outputDir: String): String {
        var strMyImagePath: String? = null
        var scaledBitmap: Bitmap? = null
        try {
            // Part 1: Decode image
            val unscaledBitmap: Bitmap =
                decodeFile(path, dstWidth, dstHeight, scaleType)
            scaledBitmap =
                if (!(unscaledBitmap.width <= dstWidth && unscaledBitmap.height <= dstHeight)) {
                    // Part 2: Scale image
                    createScaledBitmap(
                            unscaledBitmap,
                            dstWidth,
                            dstHeight,
                            ScaleType.FIT
                    )
                } else {
                    unscaledBitmap.recycle()
                    return path
                }
            val mFolder = File(outputDir)
            if (!mFolder.exists()) {
                mFolder.mkdir()
            }
            val fileName = "Scaled_${SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis())}.png"
            val file = File(mFolder.absolutePath, fileName)
            strMyImagePath = file.absolutePath
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(file)
                scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 75, fos)
                fos.flush()
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            scaledBitmap!!.recycle()
        } catch (e: Throwable) {
        }
        return strMyImagePath ?: path
    }


    private fun decodeFile(
            path: String,
            dstWidth: Int,
            dstHeight: Int,
            scaleType: ScaleType
    ): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        options.inJustDecodeBounds = false
        options.inSampleSize = calculateSampleSize(
                options.outWidth, options.outHeight, dstWidth,
                dstHeight, scaleType
        )
        return BitmapFactory.decodeFile(path, options)
    }

    /**
     * Creating a scaled version of an existing bitmap
     *
     * @param unscaledBitmap Bitmap to scale
     * @param dstWidth desired width of destination bitmap
     * @param dstHeight desired height of destination bitmap
     * @param scaleType Type of image scaling
     */
    private fun createScaledBitmap(
            unscaledBitmap: Bitmap, dstWidth: Int, dstHeight: Int,
            scaleType: ScaleType
    ): Bitmap? {
        val srcRect: Rect = calculateSrcRect(
                unscaledBitmap.width, unscaledBitmap.height,
                dstWidth, dstHeight, scaleType
        )
        val dstRect: Rect = calculateDstRect(
                unscaledBitmap.width, unscaledBitmap.height,
                dstWidth, dstHeight, scaleType
        )
        val scaledBitmap = Bitmap.createBitmap(
                dstRect.width(), dstRect.height(),
                Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(scaledBitmap)
        canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, Paint(Paint.FILTER_BITMAP_FLAG))
        return scaledBitmap
    }

    /**
     * Calculates source rectangle for scaling bitmap
     *
     * @param srcWidth Width of source image
     * @param srcHeight Height of source image
     * @param dstWidth Width of destination area
     * @param dstHeight Height of destination area
     * @param scaleType Type of scaling
     * @return Optimal source rectangle
     */
    private fun calculateSrcRect(
            srcWidth: Int, srcHeight: Int, dstWidth: Int, dstHeight: Int,
            scaleType: ScaleType
    ): Rect {
        return if (scaleType === ScaleType.CROP) {
            val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
            val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()
            if (srcAspect > dstAspect) {
                val srcRectWidth = (srcHeight * dstAspect).toInt()
                val srcRectLeft = (srcWidth - srcRectWidth) / 2
                Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight)
            } else {
                val srcRectHeight = (srcWidth / dstAspect).toInt()
                val scrRectTop = (srcHeight - srcRectHeight) / 2
                Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight)
            }
        } else {
            Rect(0, 0, srcWidth, srcHeight)
        }
    }

    /**
     * Calculates destination rectangle for scaling bitmap
     *
     * @param srcWidth Width of source image
     * @param srcHeight Height of source image
     * @param dstWidth Width of destination area
     * @param dstHeight Height of destination area
     * @param ScalingType Type of scaling
     * @return Optimal destination rectangle
     */
    private fun calculateDstRect(
            srcWidth: Int,
            srcHeight: Int,
            dstWidth: Int,
            dstHeight: Int,
            scaleType: ScaleType
    ): Rect {
        return if (scaleType === ScaleType.FIT) {
            val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
            val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()
            if (srcAspect > dstAspect) {
                Rect(0, 0, dstWidth, (dstWidth / srcAspect).toInt())
            } else {
                Rect(0, 0, (dstHeight * srcAspect).toInt(), dstHeight)
            }
        } else {
            Rect(0, 0, dstWidth, dstHeight)
        }
    }

    /**
     * @param srcWidth Width of source image
     * @param srcHeight Height of source image
     * @param dstWidth Width of destination area
     * @param dstHeight Height of destination area
     * @param scaleType Type of scaling
     * @return Optimal down scaling sample size for decoding
     */
    private fun calculateSampleSize(
            srcWidth: Int,
            srcHeight: Int,
            dstWidth: Int,
            dstHeight: Int,
            scaleType: ScaleType
    ): Int {
        return if (scaleType === ScaleType.FIT) {
            val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
            val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()
            if (srcAspect > dstAspect) {
                srcWidth / dstWidth
            } else {
                srcHeight / dstHeight
            }
        } else {
            val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
            val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()
            if (srcAspect > dstAspect) {
                srcHeight / dstHeight
            } else {
                srcWidth / dstWidth
            }
        }
    }

    enum class ScaleType {
        CROP, FIT
    }
}