package com.example.filedemo.screen

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.filedemo.LocalSnackbarProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import com.example.filedemo.component.CommonComponents.TitleRow
import com.example.filedemo.component.CommonComponents.FileItemRaw
import com.example.filedemo.utils.StorageUtils


class DocsScreen(val context: Context, val storageUtils: StorageUtils) {
    fun exportFileToDownloads(fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //Android 10 ve sonrası

            // Oluşturulacak dosyanın bilgileri hazırlanması
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "Exported_$fileName")
                put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            // MediaStore veritabanına dosyayı yazdırıyoruz
            // MediaStore bu dosya için benzersiz bir id oluşturur ve dosya sisteminde yazacağın yeri döndürür
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
           // Bu uri kullanılarak dosya kaydedilir
            uri?.let { targetUri ->
                // Yazılacak adrese bir pipeline açılır
                context.contentResolver.openOutputStream(targetUri)?.use { output ->
                    // Yazma işleminin kendisi burada yapılır
                    // (internal storage'daki dosyayı aç ve verilen adrese kopyala)
                    context.openFileInput(fileName).use { input -> input.copyTo(output) }
                }
            }
        } else {
            // Android 9 ve daha eski
            // Paylaşımlı dosya sisteminden  Downloads klasörünün yolunu al
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            // Boş dosyayı oluştur
            val targetFile = File(downloadsDir, "Exported_$fileName")

            try {
                // Dosyanın içeriğini yaz
                // Internal dosyayı aç
                context.openFileInput(fileName).use { input ->
                    // FileOutputStream ile yazmak istediğin yere pipeline aç
                    FileOutputStream(targetFile).use { output ->
                        // Internal dosya içeriğini bu pipeline üzerinden yaz
                        input.copyTo(output)
                    }
                }

                // Dosyanın oluşturullduğunu Android'e haber ver (ki veritabanı güncellensin)
                MediaScannerConnection.scanFile(context, arrayOf(targetFile.absolutePath), null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun getFileName(uri: Uri): String? {
        var result: String? = null

        // 1. Eğer URI "content://" şemasına sahipse
        if (uri.scheme == "content") {
            // Android'in file veritabanına istek gönder
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                // Gelen verilerden ilkini al
                if (cursor.moveToFirst()) {
                    // Dosya isminin tutulduğu index'i al
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        // Bu index'te tutlan string'i (name) al
                        result = cursor.getString(nameIndex)
                    }
                }
            }
        }

        // 2. Eğer "file://" şemasına sahipse
        if (result == null) {
            // Direkt stringi parçala ve filename'i dön
            result = uri.path // Uri'nin path'ini al
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    @Composable
    fun getScreen(modifier : Modifier,goHome: () -> Unit){
        getContent(modifier,goHome)
    }

    @Composable
    fun getContent(modifier : Modifier,goHome: () -> Unit){
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val importLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri?.let {
                scope.launch(Dispatchers.IO) {
                    val fileName = getFileName(it) ?: "imported_file"
                    context.contentResolver.openInputStream(it)?.use { input ->
                        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }

        val snackbar = LocalSnackbarProvider.current
        val files = remember {storageUtils.getAppSpecificPersistentFiles() }


        val deleteFileLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri ->
                uri?.let {
                    scope.launch(Dispatchers.IO) {
                        try {
                            // uri ile dosya silme
                            val isDeleted = DocumentsContract.deleteDocument(
                                context.contentResolver,
                                it
                            )

                            if (isDeleted) {
                                snackbar.showSnackbar("The file Removed.")
                            }
                        } catch (e: Exception) {
                            snackbar.showSnackbar("Error, The file could not deleted.")
                            e.printStackTrace()
                        }
                    }
                }
            }
        )

        Column(modifier) {
            TitleRow("DOCS",goHome)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { importLauncher.launch(arrayOf("*/*")) }, modifier.weight(.1f)) {
                    Text("Import")
                }
                Button(
                    onClick = {
                        deleteFileLauncher.launch(arrayOf("*/*"))
                    },
                    modifier.weight(.1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete File")
                }
            }

            LazyColumn{
                items(files.size) { index  ->
                    val file = files[index]
                    FileItemRaw(
                        file,
                        Icons.Default.ImportExport,
                        Color.Blue,
                        onClickEvent = {
                            exportFileToDownloads(file.name)
                            scope.launch {
                                snackbar.showSnackbar("File \"${file.name}\" exported to Downloads file")
                            }
                        })
                }
            }
        }
    }
}