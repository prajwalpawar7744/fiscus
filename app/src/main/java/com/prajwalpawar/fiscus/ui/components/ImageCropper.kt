package com.prajwalpawar.fiscus.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

@Composable
fun ImageCropperDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onCropped: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            bitmap = BitmapFactory.decodeStream(inputStream)
        }
    }

    if (bitmap != null) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Surface(Modifier.fillMaxSize(), color = Color.Black) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Cancel", tint = Color.White)
                        }
                        Text(
                            "Crop Photo",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Box(Modifier.size(48.dp))
                    }

                    Box(Modifier.weight(1f)) {
                        ImageCropper(bitmap!!, onCropConfirmed = onCropped)
                    }
                }
            }
        }
    }
}

@Composable
fun ImageCropper(
    bitmap: Bitmap,
    onCropConfirmed: (Bitmap) -> Unit
) {
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Matrix to track image transformation (pan/zoom)
    val imageMatrix = remember { Matrix() }
    var transformTrigger by remember { mutableLongStateOf(0L) }

    // Circle transformation (movable crop area)
    var circleCenter by remember { mutableStateOf(Offset.Zero) }
    var circleRadius by remember { mutableFloatStateOf(0f) }

    // Initialize positions
    LaunchedEffect(canvasSize) {
        if (canvasSize != Size.Zero && circleRadius == 0f) {
            val imgW = bitmap.width.toFloat()
            val imgH = bitmap.height.toFloat()

            // Initial circle at center
            circleCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            circleRadius = min(canvasSize.width, canvasSize.height) * 0.4f

            // Initial image centered and filling canvas
            val baseScale = max(canvasSize.width / imgW, canvasSize.height / imgH)
            imageMatrix.setTranslate(
                (canvasSize.width - imgW) / 2f,
                (canvasSize.height - imgH) / 2f
            )
            imageMatrix.postScale(
                baseScale,
                baseScale,
                canvasSize.width / 2f,
                canvasSize.height / 2f
            )
            transformTrigger++
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(bitmap, canvasSize) {
                if (canvasSize == Size.Zero) return@pointerInput

                detectTransformGestures { centroid, pan, zoom, _ ->
                    val circleRadius = min(canvasSize.width, canvasSize.height) * 0.4f
                    val circleCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)

                    val workingMatrix = Matrix(imageMatrix)

                    // 1. Apply zoom
                    if (zoom != 1f) {
                        workingMatrix.postScale(zoom, zoom, centroid.x, centroid.y)
                    }

                    // 2. Apply pan
                    workingMatrix.postTranslate(pan.x, pan.y)

                    // 3. Constrain to bounds
                    val imageRect = android.graphics.RectF(
                        0f,
                        0f,
                        bitmap.width.toFloat(),
                        bitmap.height.toFloat()
                    )
                    workingMatrix.mapRect(imageRect)

                    val cropLeft = circleCenter.x - circleRadius
                    val cropRight = circleCenter.x + circleRadius
                    val cropTop = circleCenter.y - circleRadius
                    val cropBottom = circleCenter.y + circleRadius

                    // If image is smaller than crop circle, scale it up to fit
                    if (imageRect.width() < (cropRight - cropLeft) || imageRect.height() < (cropBottom - cropTop)) {
                        val scaleX = (cropRight - cropLeft) / imageRect.width()
                        val scaleY = (cropBottom - cropTop) / imageRect.height()
                        val scale = max(scaleX, scaleY)
                        workingMatrix.postScale(scale, scale, circleCenter.x, circleCenter.y)

                        // Re-map after scaling
                        imageRect.set(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                        workingMatrix.mapRect(imageRect)
                    }

                    // Clamp translation
                    var dx = 0f
                    var dy = 0f

                    if (imageRect.left > cropLeft) dx = cropLeft - imageRect.left
                    else if (imageRect.right < cropRight) dx = cropRight - imageRect.right

                    if (imageRect.top > cropTop) dy = cropTop - imageRect.top
                    else if (imageRect.bottom < cropBottom) dy = cropBottom - imageRect.bottom

                    workingMatrix.postTranslate(dx, dy)

                    imageMatrix.set(workingMatrix)
                    transformTrigger++
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            if (canvasSize != size) {
                canvasSize = size
            }

            // Trigger redraw on matrix change
            transformTrigger

            if (circleRadius > 0) {
                // Draw image
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        isFilterBitmap = true
                        isDither = true
                        isAntiAlias = true
                    }
                    canvas.nativeCanvas.drawBitmap(bitmap, imageMatrix, paint)
                }

                // Dark overlay with cutout
                val path = Path().apply {
                    addOval(Rect(circleCenter, circleRadius))
                }
                clipPath(path, ClipOp.Difference) {
                    drawRect(Color.Black.copy(alpha = 0.7f))
                }

                // Crop area border
                drawCircle(
                    Color.White,
                    circleRadius,
                    circleCenter,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        FloatingActionButton(
            onClick = {
                val cropped = cropBitmap(bitmap, imageMatrix, circleCenter, circleRadius)
                onCropConfirmed(cropped)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(48.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Check, "Confirm")
        }
    }
}

private fun cropBitmap(
    bitmap: Bitmap,
    imageMatrix: Matrix,
    circleCenter: Offset,
    circleRadius: Float
): Bitmap {
    val cropSize = (circleRadius * 2).toInt()
    val output = createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(output)

    // We want the part of the image currently at (circleCenter - radius) on screen
    // to map to (0,0) in our new bitmap.

    val finalMatrix = Matrix(imageMatrix)
    // Shift the screen coordinates so that the top-left of the crop circle is the origin
    finalMatrix.postTranslate(-(circleCenter.x - circleRadius), -(circleCenter.y - circleRadius))

    val paint = android.graphics.Paint().apply {
        isFilterBitmap = true
        isDither = true
        isAntiAlias = true
    }

    canvas.drawBitmap(bitmap, finalMatrix, paint)
    return output
}
