package com.skypedogg.cameraimagechooser

import android.content.Context
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class ImageUtils {

    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss"

    fun getFileFromUri(uri: Uri?): File? = uri?.path?.let { File(it) }

    fun getPathFromUri(uri: Uri?): String? = uri?.path

    fun getAbsolutePathFromUri(context: Context, uri: Uri?): String? = uri?.let { getAbsolutePathFromURI(context, it) }

    fun getBitmapFromPath(absolutePath: String): Bitmap = BitmapFactory.decodeFile(absolutePath)

    fun getBitmapFromFile(file: File): Bitmap = BitmapFactory.decodeFile(file.absolutePath)

    private fun getAbsolutePathFromURI(context: Context, contentURI: Uri): String? {
        var absolutePath: String? = null
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = context.contentResolver.query(contentURI, filePathColumn, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(filePathColumn[0])
                absolutePath = it.getString(columnIndex)
            }
            it.close()
        }
        return absolutePath
    }


    fun File.compressImage(size: Double): File {
        val outputStream = ByteArrayOutputStream()
        val givenSize: Long = (size * 1024 * 1024).toLong()
        val bitmap = getBitmapFromFile(this)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        this.writeBytes(outputStream.toByteArray())
        if (this.length() > givenSize)
            return this.compressImage(size)
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
            val fileName = "Scaled_${SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis())}.jpg"
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
            e.printStackTrace()
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