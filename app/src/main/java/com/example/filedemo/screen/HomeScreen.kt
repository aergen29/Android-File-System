package com.example.filedemo.screen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class HomeScreen(val context: Context) {


    @Composable
    fun getScreen(modifier: Modifier, changePage:(String)->Unit){
        getContent(modifier, changePage)
    }
    @Composable
    fun getContent(modifier: Modifier, changePage:(String)->Unit){
        val pages = arrayOf(
            "App Specific - Internal" to  "appspecificinternal",
            "App Specific - External" to  "appspecificexternal",
            "Media" to "media",
            "Docs" to "docs")
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Select Page", style = MaterialTheme.typography.headlineLarge)
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                pages.forEach { (name, path) ->
                    Button(
                        modifier= Modifier.fillMaxWidth(0.8f).padding(8.dp),
                        onClick = { changePage(path) }) {
                        Text(name)
                    }
                }
            }
        }
    }
}