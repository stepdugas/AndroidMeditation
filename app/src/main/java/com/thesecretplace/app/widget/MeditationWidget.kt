package com.thesecretplace.app.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.thesecretplace.app.MainActivity

class MeditationWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences("the_secret_place_prefs", Context.MODE_PRIVATE)
        val streakCount = prefs.getInt("streakCount", 0)
        val userName = prefs.getString("userName", "") ?: ""

        val goldColor = Color(0xFFC9A84C)
        val lavenderColor = Color(0xFFB8A9D4)
        val navyColor = Color(0xFF1A1F35)

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(navyColor)
                    .padding(16.dp)
                    .clickable(
                        actionStartActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("meditationapp://meditate"))
                                .setClass(context, MainActivity::class.java)
                        )
                    )
            ) {
                Text(
                    text = "The Secret Place",
                    style = TextStyle(
                        color = ColorProvider(goldColor),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = "$streakCount Day Streak",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (userName.isNotEmpty()) {
                    Text(
                        text = "Hi, ${userName.replaceFirstChar { it.uppercase() }}",
                        style = TextStyle(
                            color = ColorProvider(lavenderColor),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

class MeditationWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MeditationWidget()
}
