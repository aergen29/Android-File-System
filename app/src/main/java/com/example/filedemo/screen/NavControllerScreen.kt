package com.example.filedemo.screen

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.filedemo.utils.StorageUtils

class NavControllerScreen(val context: Context) {

    @Composable
    fun getScreen(modifier: Modifier){
        val navController = rememberNavController()
        val storageUtils: StorageUtils = StorageUtils(context)
        NavHost(navController, startDestination = "home") {
            composable ("home"){
                HomeScreen(context).getScreen(modifier) { newPage ->
                    navController.navigate(newPage)
                }
            }
            composable ("appspecificinternal") {
                AppSpecificScreen(context,storageUtils).getScreen(modifier, false){
                    navController.navigate("home"){
                        popUpTo("appspecificinternal") { inclusive = true }
                    }
                }
            }
            composable ("appspecificexternal") {
                AppSpecificScreen(context,storageUtils).getScreen(modifier, true){
                    navController.navigate("home"){
                        popUpTo("appspecificexternal") { inclusive = true }
                    }
                }
            }
            composable ("media") {
                MediaScreen(context).getScreen(modifier){
                    navController.navigate("home"){
                        popUpTo("media") { inclusive = true }
                    }
                }
            }
            composable ("docs") {
                DocsScreen(context, storageUtils).getScreen(modifier){
                    navController.navigate("home"){
                        popUpTo("docs") { inclusive = true }
                    }
                }
            }
        }
    }
}