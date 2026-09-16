package kz.spatz.pictureswithintents

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas

/**
 * Процедурный будильник, нарисованный на Canvas:
 * демонстрирует дуги, кубические кривые Безье, радиальные/линейные заливки, прямые и текст через nativeCanvas.
 */
@Composable
fun ProceduralCanvasAlarm(modifier: Modifier = Modifier.Companion) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = minOf(w, h) * 0.32f

        // 1. ПРЯМЫЕ ЛИНИИ (Ножки будильника)
        val strokeThick = 14f
        drawLine(
            color = Color(0xFF37474F),
            start = Offset(center.x - radius * 0.7f, center.y + radius * 0.7f),
            end = Offset(center.x - radius * 1.1f, center.y + radius * 1.2f),
            strokeWidth = strokeThick,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF37474F),
            start = Offset(center.x + radius * 0.7f, center.y + radius * 0.7f),
            end = Offset(center.x + radius * 1.1f, center.y + radius * 1.2f),
            strokeWidth = strokeThick,
            cap = StrokeCap.Round
        )

        // 2. ДУГИ (Чашечки механических звонков сверху)
        val bellSize = radius * 0.8f
        drawArc(
            color = Color(0xFFD32F2F),
            startAngle = 170f,
            sweepAngle = 160f,
            useCenter = true,
            topLeft = Offset(center.x - radius * 1.2f, center.y - radius * 1.25f),
            size = Size(bellSize, bellSize)
        )
        drawArc(
            color = Color(0xFFD32F2F),
            startAngle = 210f,
            sweepAngle = 160f,
            useCenter = true,
            topLeft = Offset(center.x + radius * 0.4f, center.y - radius * 1.25f),
            size = Size(bellSize, bellSize)
        )

        // 3. ГЛАДКИЕ КРИВЫЕ (Ручка между звонками через CubicTo)
        val handlePath = Path().apply {
            moveTo(center.x - radius * 0.45f, center.y - radius * 0.75f)
            cubicTo(
                center.x - radius * 0.3f, center.y - radius * 1.4f,
                center.x + radius * 0.3f, center.y - radius * 1.4f,
                center.x + radius * 0.45f, center.y - radius * 0.75f
            )
        }
        drawPath(
            path = handlePath,
            color = Color(0xFF78909C),
            style = Stroke(width = 10f, cap = StrokeCap.Round)
        )

        // 4. ГРАДИЕНТНАЯ ЗАЛИВКА (Корпус часов с радиальным градиентом)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF5252), Color(0xFFB71C1C)),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )

        // Циферблат
        val innerRadius = radius * 0.82f
        drawCircle(
            color = Color.White,
            radius = innerRadius,
            center = center
        )

        // 5. ДУГА-ИНДИКАТОР ТАЙМЕРА (Сегмент циферблата)
        drawArc(
            color = Color(0xFFFFCDD2),
            startAngle = -90f,
            sweepAngle = 110f,
            useCenter = true,
            topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
            size = Size(innerRadius * 2, innerRadius * 2)
        )

        // Стрелки
        drawLine(
            color = Color(0xFF212121),
            start = center,
            end = Offset(center.x, center.y - innerRadius * 0.65f),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF212121),
            start = center,
            end = Offset(center.x + innerRadius * 0.45f, center.y),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )
        drawCircle(color = Color(0xFFD32F2F), radius = 7f, center = center)

        // 6. ТЕКСТ (Рисование через Native Android Canvas)
        drawContext.canvas.nativeCanvas.apply {
            val paint = Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = radius * 0.22f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            drawText("12:00", center.x, center.y + innerRadius * 0.5f, paint)
        }
    }
}