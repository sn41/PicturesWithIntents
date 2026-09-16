package kz.spatz.pictureswithintents

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlarmShowcaseScreen()
                }
            }
        }
    }
}

/**
 * Кастомный Modifier для наложения анимированного блика (шейдера-маски).
 * BlendMode.SrcAtop переносит градиент только на непрозрачные пиксели дочернего контента.
 */
fun Modifier.shimmerSheenMask(): Modifier = this.then(
    Modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            drawContent()
        }
)


