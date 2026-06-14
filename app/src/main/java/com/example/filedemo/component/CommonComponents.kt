package com.example.filedemo.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.io.File

object CommonComponents {
    @Composable
    fun TitleRow(title:String,goHome:()->Unit){
        Row(verticalAlignment = Alignment.Companion.CenterVertically) {
            IconButton(onClick = { goHome() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null
                )
            }
            Text(title, style = MaterialTheme.typography.headlineLarge)

        }
    }

    @Composable
    fun TextArea(text:String,onTextChange: (String)->Unit) {

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text("Dosya içeriği") },
            modifier = Modifier.Companion
                .fillMaxWidth()
                .height(150.dp),
            singleLine = false,
            minLines = 5,
            maxLines = 10,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SingleSelectDropdown(label: String,
                             options: List<String>,
                             modifier: Modifier,
                             onSelectionChange: (String)->Unit) {
        var expanded by remember { mutableStateOf(false) }
        var selectedOptionText by remember { mutableStateOf(options[0]) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = modifier,
        ) {
            // The text field that triggers the dropdown
            OutlinedTextField(
                value = selectedOptionText,
                onValueChange = {},
                readOnly = true, // Set to true to prevent manual typing
                label = { Text(label) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.Companion.menuAnchor().fillMaxWidth()
            )

            // The actual dropdown menu
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            selectedOptionText = selectionOption
                            expanded = false
                            onSelectionChange(selectionOption)
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun FileItemRaw(file: File, icon: ImageVector, color:Color, onClickEvent:() -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically)
                {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = file.name, fontWeight = FontWeight.Bold)
                        Text(text = "${file.length()}B")
                    }

                    IconButton(onClick = onClickEvent) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Delete",
                            tint = color
                        )
                    }
                }
            }
        }
    }


    @Composable
    fun FileItemRow(file: File, onDelete:() -> Unit) {
        FileItemRaw(file,Icons.Default.Delete, Color.Red,onDelete)
    }

}