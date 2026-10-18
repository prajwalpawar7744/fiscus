package com.prajwalpawar.fiscus.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min

// The profile picture lives in app-private storage: a Photo Picker URI stops working once
// the app restarts, so the picked image is copied (cropped square, downscaled) here.
class ProfilePhotoStore(
    private val context: Context
) {
    // Read by backups; restores write it through [saveBytes].
    val file: File
        get() = File(context.filesDir, FILE_NAME)

    suspend fun saveBytes(bytes: ByteArray) = withContext(Dispatchers.IO) {
        file.writeBytes(bytes)
    }

    suspend fun save(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val square = decode(uri).centerSquare()
            val scaled = if (square.width > SIZE_PX) {
                Bitmap.createScaledBitmap(square, SIZE_PX, SIZE_PX, true)
            } else {
                square
            }
            // Write then rename, so a failed save never leaves a half-written photo.
            val temp = File(context.filesDir, "$FILE_NAME.tmp")
            temp.outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, it) }
            temp.renameTo(file)
        }.getOrDefault(false)
    }

    suspend fun load(): Bitmap? = withContext(Dispatchers.IO) {
        file.takeIf { it.exists() }?.let { BitmapFactory.decodeFile(it.path) }
    }

    suspend fun delete() = withContext(Dispatchers.IO) {
        file.delete()
    }

    private fun decode(uri: Uri): Bitmap =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder applies EXIF rotation and can downsample while decoding.
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, uri)
            ) { decoder, info, _ ->
                val shortSide = min(info.size.width, info.size.height)
                if (shortSide > SIZE_PX) {
                    val scale = SIZE_PX.toFloat() / shortSide
                    decoder.setTargetSize(
                        (info.size.width * scale).toInt(),
                        (info.size.height * scale).toInt()
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            var sample = 1
            while (min(bounds.outWidth, bounds.outHeight) / (sample * 2) >= SIZE_PX) sample *= 2
            context.contentResolver.openInputStream(uri)!!.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            }!!
        }

    private fun Bitmap.centerSquare(): Bitmap {
        val side = min(width, height)
        if (width == height) return this
        return Bitmap.createBitmap(this, (width - side) / 2, (height - side) / 2, side, side)
    }

    private companion object {
        const val FILE_NAME = "profile.jpg"
        const val SIZE_PX = 512
        const val QUALITY = 90
    }
}
