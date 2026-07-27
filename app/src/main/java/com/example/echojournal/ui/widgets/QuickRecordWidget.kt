package com.example.echojournal.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.echojournal.MainActivity

class QuickRecordWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                QuickRecordContent()
            }
        }
    }

    @Composable
    private fun QuickRecordContent() {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Circle glass background
            Box(
                modifier = GlanceModifier
                    .size(80.dp)
                    .padding(8.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Echo",
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp)
                    )
                    Text(
                        text = "REC",
                        style = TextStyle(color = ColorProvider(Color.White.copy(alpha = 0.5f)), fontSize = 10.sp)
                    )
                }
            }
        }
    }
}

class QuickRecordWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickRecordWidget()
}
