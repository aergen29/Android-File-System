package com.example.filedemo.screen

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.filedemo.LocalSnackbarProvider
import com.example.filedemo.utils.StorageUtils
import kotlinx.coroutines.launch
import com.example.filedemo.component.CommonComponents.SingleSelectDropdown
import com.example.filedemo.component.CommonComponents.TitleRow
import com.example.filedemo.component.CommonComponents.TextArea
import com.example.filedemo.component.CommonComponents.FileItemRow
import java.io.File


class AppSpecificScreen(val context: Context, val storageUtils: StorageUtils) {



    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun getScreen(modifier : Modifier, isExternal: Boolean, goHome: () -> Unit){
        getContent(modifier, isExternal, goHome)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun getContent(modifier : Modifier, isExternal: Boolean, goHome: () -> Unit){
        var filename by remember { mutableStateOf("") }
        var fileContents by remember {mutableStateOf("")}
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        var fileType by remember { mutableStateOf("Persistent") }
        val tabs = listOf("List", "Cache Files","Create",)

        val snackbar = LocalSnackbarProvider.current
        val scope = rememberCoroutineScope()



        Column(modifier) {
            TitleRow("App Specific - ${if (isExternal) "External" else "Internal"}",goHome)
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
                    )
                }
            }
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp)) {
                Spacer(modifier = Modifier.height(8.dp))
                when (selectedTabIndex) {
                    0 -> {
                        val files = remember { storageUtils.getAppSpecificPersistentFiles(isExternal).toMutableList()}
                        Text(if (isExternal) context.getExternalFilesDir(null).toString() else context.filesDir.toString())
                        Text("Total Allocatable Bytes ${storageUtils.getAppSpecificPersistentLimit()/(1024*1024)} MB")
                        LazyColumn{
                            items(files.size) { index  ->
                                val file = files[index]
                                FileItemRow(file, onDelete = {
                                    if(!isExternal) {
                                        // Context kısayolunu kullanabilirz
                                        context.deleteFile(file.name)
                                        files.remove(file)
                                    }
                                    else{
                                        // Context kısayolu işe yaramaz java'nın file class'ını kullanmak zorundayız
                                        val dir = context.getExternalFilesDir(null)
                                        val file = File(dir, file.name)
                                        if(file.exists()) file.delete()
                                        files.remove(file)
                                    }
                                })
                            }
                        }
                    }
                    1-> {
                        val cacheFiles = remember { storageUtils.getAppSpecificCacheFiles(isExternal).toMutableList()}
                        Text(if(isExternal) context.externalCacheDir.toString() else context.cacheDir.toString())
                        Text("Total Allocatable Bytes ${storageUtils.getAppSpecificCacheLimit()/(1024*1024)} MB")
                        LazyColumn{
                            items(cacheFiles.size) { index  ->
                                val file = cacheFiles[index]
                                FileItemRow(file, onDelete = {
                                    if(!isExternal) {
                                        // Context kısayolunu kullanabilirz
                                        context.deleteFile(file.name)
                                        cacheFiles.remove(file)
                                    }
                                    else{
                                        // Context kısayolu işe yaramaz java'nın file class'ını kullanmak zorundayız
                                        val dir = context.getExternalFilesDir(null)
                                        val file = File(dir, file.name)
                                        if(file.exists()) file.delete()
                                        cacheFiles.remove(file)
                                    }
                                })
                            }
                        }
                    }
                    2 -> {
                        TextField(
                            label = { Text("File Name") },
                            value = filename,
                            onValueChange = {
                                filename = it
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SingleSelectDropdown(
                            "File Type",
                            listOf("Persistent", "Cache"),
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)) {
                            fileType = it
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextArea(fileContents,{fileContents=it})
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                modifier = Modifier
                                    .padding(5.dp)
                                    .fillMaxWidth(0.8f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.5.dp, Color(0xFF6200EE)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6200EE)),
                                onClick = {
                                    val savedName = filename
                                    if (fileType == "Cache") {
                                        storageUtils.writeCacheFile(filename,fileContents, isExternal)
                                    } else {
                                        storageUtils.writePersistentFile(filename,fileContents, isExternal)
                                    }
                                    scope.launch {
                                        snackbar.showSnackbar("$fileType \"${savedName}\" Saved Successfully!")
                                    }
                                    filename = ""
                                    fileContents = ""
                                }) {
                                Text("Create")
                            }
                        }
                    }

                }
            }


        }
    }
}