package com.example.echojournal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PinSetupDialog(
    isLiquid: Boolean,
    onDismiss: () -> Unit,
    onPinSet: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Allow us to center properly
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .width(340.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(32.dp),
                isLiquid = isLiquid
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Установите PIN", 
                        color = Color.White, 
                        fontSize = 22.sp, 
                        fontWeight = FontWeight.ExtraBold
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        repeat(4) { index ->
                            val filled = index < pin.length
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(if (filled) Color.White else Color.White.copy(alpha = 0.2f))
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // Numeric keypad
                    val digits = listOf(
                        listOf("1","2","3"), 
                        listOf("4","5","6"), 
                        listOf("7","8","9"), 
                        listOf("C","0","OK")
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        digits.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                row.forEach { digit ->
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .glassIcon(isLiquid = isLiquid)
                                            .clickable {
                                                when (digit) {
                                                    "C" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                                    "OK" -> if (pin.length == 4) { onPinSet(pin); onDismiss() }
                                                    else -> if (pin.length < 4) pin += digit
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = digit, 
                                            color = Color.White, 
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
