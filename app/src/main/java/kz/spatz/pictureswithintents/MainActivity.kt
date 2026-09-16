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

/*
@Composable
fun AlarmShowcaseScreen() {
    val context = LocalContext.current

    // Лаунчер для получения результата из другого Activity
    val resultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val returnedData = result.data?.getStringExtra("EXTRA_RESULT_TIME")
            Toast.makeText(context, "Ответ: $returnedData", Toast.LENGTH_LONG).show()
        }
    }

    // Анимация пробегания блика при старте
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -800f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SheenOffset"
    )

    // Общая колонка с прокруткой и наложением световой маски
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                // Накладываем линейный градиент блика поверх всего содержимого в режиме SrcAtop
                val brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.65f),
                        Color.Transparent
                    ),
                    start = Offset(translateAnim, translateAnim),
                    end = Offset(translateAnim + 300f, translateAnim + 300f)
                )
                drawRect(
                    brush = brush,
                    blendMode = BlendMode.SrcAtop
                )
            },
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // КНОПКА 1: PNG Растр -> Неявный Intent (Системные часы/будильник)
        AlarmCardButton(
            title = "1. PNG Bitmap: Системный будильник",
            onClick = {
                val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_MESSAGE, "Подъём из приложения!")
                    putExtra(AlarmClock.EXTRA_HOUR, 8)
                    putExtra(AlarmClock.EXTRA_MINUTES, 0)
                    putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                }
                safeStartIntent(context, intent)
            }
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.alarm_raster),
                contentDescription = "PNG Alarm",
                modifier = Modifier.fillMaxSize()
            )
        }

        // КНОПКА 2: Иконка (ImageVector Material) -> Явный Intent на запуск другого Activity
        AlarmCardButton(
            title = "2. Material ImageVector: Явный Intent",
            onClick = {
                val intent = Intent(context, DetailResultActivity::class.java)
                context.startActivity(intent)
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Alarm,
                contentDescription = "Vector Icon Alarm",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxSize()
            )
        }

        // КНОПКА 3: Вектор из SVG (VectorDrawable XML) -> Activity с возвратом результата
        AlarmCardButton(
            title = "3. XML/SVG Vector: Activity Result",
            onClick = {
                val intent = Intent(context, DetailResultActivity::class.java)
                resultLauncher.launch(intent)
            }
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.ic_alarm_svg),
                contentDescription = "SVG Alarm",
                modifier = Modifier.fillMaxSize()
            )
        }

        // КНОПКА 4: Процедурный будильник на Canvas -> Запуск Service
        AlarmCardButton(
            title = "4. Procedural Canvas: Запуск Сервиса",
            onClick = {
                val serviceIntent = Intent(context, AlarmForegroundService::class.java)
                context.startService(serviceIntent)
            }
        ) {
            ProceduralCanvasAlarm(modifier = Modifier.fillMaxSize())
        }

        // КНОПКА 5: Custom VectorPainter -> Стороннее приложение (ACTION_MAIN / APPS)
        AlarmCardButton(
            title = "5. VectorPainter: Открытие приложения Калькулятор/Стороннего",
            onClick = {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.calculator")
                    ?: Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR)
                safeStartIntent(context, launchIntent)
            }
        ) {
            val customPainter = rememberVectorPainter(Icons.Outlined.AlarmOn)
            androidx.compose.foundation.Image(
                painter = customPainter,
                contentDescription = "Painter Alarm",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
*/

fun safeStartIntent(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Компонент не найден: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
