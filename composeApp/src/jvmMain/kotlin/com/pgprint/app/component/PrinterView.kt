package com.pgprint.app.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import pgprint.composeapp.generated.resources.Res
import pgprint.composeapp.generated.resources.compose_multiplatform
import pgprint.composeapp.generated.resources.usb

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterView(modifier: Modifier = Modifier) {


    Card {
            Text("这里是 Card")
    }




    var expanded by remember { mutableStateOf(false) }

    Image(
        painter = painterResource(Res.drawable.compose_multiplatform),
        contentDescription = "",
    )

    DropdownMenuItem(
        text = { Text("123123213", style = MaterialTheme.typography.bodyLarge) },
        onClick = {

        },
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.usb),
                contentDescription = "usb device",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = { Text("trailingIcon") },
        contentPadding = PaddingValues(5.dp) ,
    )
}