package com.example.echojournal.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.echojournal.MainActivity

class InsightWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                InsightContent()
            }
        }
    }

    @Composable
    private fun InsightContent() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Инсайт дня",
                style = TextStyle(fontSize = 12.sp)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "Запишите мысли...",
                style = TextStyle(fontSize = 14.sp)
            )
        }
    }
}

class InsightWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = InsightWidget()
}
