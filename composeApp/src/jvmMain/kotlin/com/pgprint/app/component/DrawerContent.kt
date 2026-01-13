package com.pgprint.app.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pgprint.app.model.PrintPlatform
import com.pgprint.app.utils.AppColors
import com.pgprint.app.utils.DesktopToastQueue
import org.jetbrains.compose.resources.painterResource
import pgprint.composeapp.generated.resources.Close_circle_fill
import pgprint.composeapp.generated.resources.Res


@Composable
fun DrawerContent(
    modifier: Modifier = Modifier,
    printPlatformList: List<PrintPlatform>,
    onPrint: (daySeq: String, wmId: String) -> Unit,
    onClose: () -> Unit = {}
) {

    var selected by remember {
        mutableStateOf("meituan")
    }
    var inputValue by remember {
        mutableStateOf("")
    }

    Column(
        modifier = modifier.widthIn(500.dp, 800.dp).fillMaxHeight().background(Color.White).padding(10.dp)
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "查询打印",
                fontSize = 16.sp
            )
            Icon(
                painter = painterResource(Res.drawable.Close_circle_fill),
                contentDescription = "Close_circle_fill",
                modifier = Modifier.size(22.dp).clickable(
                    onClick = onClose
                )
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp)
        )
        Column (
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SingleChoiceSegmentedButtonRow {
                printPlatformList.forEachIndexed { index, label ->
                    key(label.id) {
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = printPlatformList.size
                            ),
                            onClick = { selected = label.id },
                            selected =  label.id == selected,
                            label = { Text(label.label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 10.sp) }
                        )
                    }
                }
            }
            Row (
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    label = {
                        Text("请输入订单号/流水号", fontSize = 12.sp)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.Close_circle_fill),
                            contentDescription = "",
                            modifier = Modifier.size(20.dp).pointerHoverIcon(PointerIcon.Hand).clickable(
                                onClick = {
                                    inputValue = ""
                                }
                            ),
                            tint = Color.Gray
                        )
                    }
                )
                Button(
                    onClick = {
                        if (inputValue.isNotEmpty()) {
                            onPrint(inputValue, selected)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.PrimaryColor,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 10.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("查询打印", color = Color.White)
                }
            }
        }
    }
}