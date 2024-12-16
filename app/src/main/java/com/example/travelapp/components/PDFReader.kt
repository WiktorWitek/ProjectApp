package com.example.travelapp.components

import android.content.ContentResolver
import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.example.travelapp.data.TripData
import java.io.File
import java.io.FileOutputStream

@Composable
fun PDFReader(pdfUri: Uri) {
    val context = LocalContext.current
    val contentResolver: ContentResolver = context.contentResolver

    // Otwieramy ParcelFileDescriptor z URI
    val parcelFileDescriptor = contentResolver.openFileDescriptor(pdfUri, "r")
    parcelFileDescriptor?.let {
        val pdfRenderer = PdfRenderer(it)

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(pdfRenderer.pageCount) { index ->
                val page = pdfRenderer.openPage(index)
                val bitmap = android.graphics.Bitmap.createBitmap(
                    page.width,
                    page.height,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                var scale by remember { mutableStateOf(1f) } // Skalowanie
                var offsetX by remember { mutableStateOf(0f) } // Przesunięcie X
                var offsetY by remember { mutableStateOf(0f) } // Przesunięcie Y

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f) // Ograniczenie zoomu
                                offsetX += pan.x
                                offsetY += pan.y
                            }
                        }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "PDF page number: $index",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Zamykamy renderer po zakończeniu
        DisposableEffect(Unit) {
            onDispose {
                pdfRenderer.close()
                parcelFileDescriptor.close()
            }
        }
    }
}


fun loadTickets(trip: TripData?): List<String> {
    return try {
        trip?.tickets?.keys?.toList() ?: emptyList()
    } catch (e: Exception) {
        Log.e("Tickets", "Error loading tickets: ${e.message}")
        emptyList()
    }
}

fun saveFileToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
    return try {
        val appDir = context.filesDir
        val file = File(appDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } != -1) {
                    outputStream.write(buffer, 0, length)
                }
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun isFileAccessible(uri: Uri): Boolean {
    return File(uri.path).exists()
}