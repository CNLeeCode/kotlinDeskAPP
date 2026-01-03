package com.pgprint.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pgprint.app.router.LocalNetworkStatus
import com.pgprint.app.router.component.LoginComponent
import com.pgprint.app.utils.AppColors
import com.pgprint.app.utils.DataStored
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.resources.painterResource
import pgprint.composeapp.generated.resources.Res
import pgprint.composeapp.generated.resources.undraw_winter_walk


@Composable
fun Login(
    component: LoginComponent,
    modifier: Modifier = Modifier,
) {
    val localNetworkStatus = LocalNetworkStatus.current
    val textFieldState = rememberTextFieldState("")
    var loading by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(true) {
        DataStored.shopIdFlow.filter { it.isNotEmpty() }.collect { storedShopId ->
            textFieldState.setTextAndPlaceCursorAtEnd(storedShopId)
            loading = true
            delay(1000)
            component.toHomeAction(storedShopId)
        }
    }

    fun toHomePage () {
        if (textFieldState.text.toString().isNotEmpty()) {
            loading = true
            component.toHomeAction(textFieldState.text.toString())
        }
    }


    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(Res.drawable.undraw_winter_walk),
            contentDescription = "login",
            modifier = Modifier.width(400.dp).align(Alignment.BottomEnd)
        )
        Column (
            modifier = Modifier.align(Alignment.Center).offset(y = (-100).dp).width(500.dp)
                .background(Color.White)
                .clip(RoundedCornerShape(6.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "请输入门店号",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                state = textFieldState,
                placeholder = { Text(text = "门店号") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.onPreviewKeyEvent { event ->
                    if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                        toHomePage()
                        true
                    } else false
                },
                lineLimits = TextFieldLineLimits.SingleLine,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = ::toHomePage,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(280.dp)
                    .height(40.dp)
                    .background(AppColors.PrimaryColor),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.PrimaryColor
                )
            ) {
                Text(
                    text = if (loading) "跳转中..." else  "输入完成",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(localNetworkStatus.message, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
