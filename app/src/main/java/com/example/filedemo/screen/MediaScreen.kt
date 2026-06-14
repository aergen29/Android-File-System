package com.example.filedemo.screen

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.filedemo.LocalSnackbarProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.filedemo.component.CommonComponents.TitleRow

class MediaScreen(val context: Context) {

    suspend fun createAndSaveSolidColorImage(
        color: Color,
        colorName: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // CREATEING IMAGE
            val bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                this.color = color.toArgb()
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, 500f, 500f, paint)

            // SAVING

            // Unique filename
            val fileName = "${colorName.lowercase()}_${System.currentTimeMillis()}.jpg"


            // DATABASE VALUES CREATE
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ColorDemo")
                }
            }

            // veritabanından yer ayırt
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )

            // eğer geçerli bir yer varsa (yer ayırma işlemi başarılıysa)
            uri?.let {
                // Dosyaya veri yaz
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                }
                bitmap.recycle()
                return@withContext true
            }
            return@withContext false

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    fun fetchImageNames(context: Context): List<Long> {
        val ids = mutableListOf<Long>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        context.contentResolver.query(
            uri,
            projection,
            null,null,null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()){
                ids.add(cursor.getLong(idCol))
            }
        }

        return ids
    }

    @Composable
    fun MediaImageItem(
        imageId:Long,
        contentDescription: String?
    ) {
        val context = LocalContext.current

        var name by remember { mutableStateOf("") }
        var size by remember { mutableStateOf<Long>(0) }
        var mime_type by remember { mutableStateOf("")}
        var uri by remember { mutableStateOf(Uri.EMPTY)}



        LaunchedEffect(imageId) {
            // imageId'den uri'ye eriş (medya dosyalarının adresi + imageId)
            val imageUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageId
            )

            uri = imageUri

            // Veritabanından hangi verilerini isteyeceğiz
            val projection = arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.MIME_TYPE,
            )


            withContext(Dispatchers.IO) {
                // veritabanına sorgu at ve dönen verileri al
                context.contentResolver.query(imageUri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                        val mimeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

                        name = cursor.getString(nameIndex)
                        size = cursor.getLong(sizeIndex)
                        mime_type = cursor.getString(mimeIndex)
                    }
                }
            }
        }

        // byte to MB or KB
        val readableSize = when {
            size >= 1024 * 1024 -> "${String.format("%.2f", size / (1024f * 1024f))} MB"
            size >= 1024 -> "${size / 1024} KB"
            else -> "$size B"
        }

        val scope = rememberCoroutineScope()
        val snackbar = LocalSnackbarProvider.current

        val deleteLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ){ }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                AsyncImage(
                    model = uri,
                    contentDescription = name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            IconButton(onClick = {
                                // DELETING MEDIA
                                // Android 11 ve sonrası
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    // Delete Request'i oluştur
                                    val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, listOf(uri))
                                    // Oluşturulan isteği intent'e ver
                                    val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                                    // İntent üzerinden isteği çalıştır
                                    deleteLauncher.launch(request)
                                }
                                // Android 10 ve öncesi
                                else {
                                    val deletedRows = context.contentResolver.delete(uri, null, null)
                                    if (deletedRows > 0) {
                                        scope.launch { snackbar.showSnackbar("File Deleted") }
                                    }
                                }

                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun getScreen(modifier : Modifier,goHome: () -> Unit){
        var imageIds by remember { mutableStateOf(listOf<Long>()) }

        // GET IMAGE IDS
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                imageIds = fetchImageNames(context)
            }
        }
        // MEDIA PERMISSION
        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        }
        getContent(modifier,imageIds,goHome)
    }

    @Composable
    fun getContent(modifier : Modifier, imageIds: List<Long>, goHome: () -> Unit){
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        val colors = listOf(
            "Red" to Color.Red,
            "Blue" to Color.Blue,
            "Yellow" to Color.Yellow
        )

        val snackbar = LocalSnackbarProvider.current

        Column(modifier) {
            TitleRow("Media",goHome)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                colors.forEach { (name, color) ->
                    Button(
                        onClick = {
                            // CREATE BUTTON ONCLICK
                            scope.launch {
                                val isSuccess = createAndSaveSolidColorImage(
                                    color = color,
                                    colorName = name
                                )
                                if (isSuccess) {
                                    snackbar.showSnackbar("$name saved successfully")
                                } else {
                                    snackbar.showSnackbar("Error.")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = color),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = name,
                            color = if (color == Color.Yellow) Color.Black else Color.White
                        )
                    }
                }
            }

            LazyColumn{
                items(imageIds.size) { index  ->
                    val fileId = imageIds[index]
                    MediaImageItem(fileId,null)
                }
            }
        }
    }

}