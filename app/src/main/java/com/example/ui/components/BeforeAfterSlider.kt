package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BeforeAfterSlider(
    diseaseText: String,
    healthyText: String,
    modifier: Modifier = Modifier,
    beforeContent: @Composable () -> Unit,
    afterContent: @Composable () -> Unit,
) {
    var width by remember { mutableStateOf(0) }
    var dragX by remember { mutableStateOf(0f) }

    // Position the handle divider in the middle when size changes initially
    LaunchedEffect(width) {
        if (width > 0 && dragX == 0f) {
            dragX = width / 2f
        }
    }

    val percentage = if (width > 0) (dragX / width).coerceIn(0f, 1f) else 0.5f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.DarkGray)
            .onSizeChanged { width = it.width }
    ) {
        // Bottom (After) layer showing healthy, healed restoration
        Box(modifier = Modifier.fillMaxSize()) {
            afterContent()

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = healthyText,
                    color = Color(0xFF81C784),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Top (Before) layer showing active disease infection, clipped dynamically
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    val clipWidth = size.width * percentage
                    clipRect(right = clipWidth) {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            beforeContent()

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = diseaseText,
                    color = Color(0xFFE57373),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Drag handle line overlay
        if (width > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(Color.White)
                    .offset { IntOffset((dragX.toInt() - 2).coerceIn(0, width - 4), 0) }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            dragX = (dragX + dragAmount.x).coerceIn(0f, width.toFloat())
                        }
                    }
            ) {
                // Drag thumb button
                Box(
                    modifier = Modifier
                        .size(28.dp, 44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .align(Alignment.Center)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(2.dp, 18.dp).background(Color(0xFF81C784)))
                        Spacer(modifier = Modifier.width(3.dp))
                        Box(modifier = Modifier.size(2.dp, 18.dp).background(Color(0xFF81C784)))
                    }
                }
            }
        }
    }
}
