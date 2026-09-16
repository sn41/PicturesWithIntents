package kz.spatz.pictureswithintents

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.outlined.AlarmOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

fun safeStartIntent(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Компонент не найден: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}


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

    //!!!!  Анимация пробегания блика при старте
    // Бесконечный переход
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")

    // Изменяем смещение заливки (смещение градиента)
    val translateAnim by transition.animateFloat(
        initialValue = -800f,
        targetValue = 2000f,
        // Повторять бесконечно
        animationSpec = infiniteRepeatable(
            // Замедление по кривой
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            // При повторении - начать с начала
            repeatMode = RepeatMode.Restart
        ),
        // Метка для отслеживания состояния
        label = "SheenOffset"
    )

    // Общая колонка с прокруткой и наложением световой маски
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())

            // Modifier.Element, который обеспечивает отрисовку контента в отдельном слое отрисовки (draw layer).
            // Этот слой можно обновлять (помечать как недействительный) независимо от родительских элементов.
            // Использование graphicsLayer рекомендуется в тех случаях, когда контент обновляется независимо от расположенных выше элементов,
            // чтобы минимизировать объем перерисовываемой области.
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)

            // Создает DrawModifier, позволяющий разработчику выполнять отрисовку до или после содержимого макета.
            // Кроме того, этот модификатор позволяет изменять холст (canvas) макета.
            .drawWithContent {

                // Вызывает выполнение операций рисования дочерних элементов во время выполнения лямбда-выражения onPaint.
                // Нарисовать то что содержится в колонке, чтобы поверх нарисовать блик
                drawContent()

                // Накладываем линейный градиент блика поверх всего содержимого в режиме SrcAtop
                // размер 300х300 градиент по диагонали
                val brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.65f),
                        Color.Transparent
                    ),
                    // Начало градиента - определяется смещением (translateAnim)
                    start = Offset(translateAnim, translateAnim),
                    end = Offset(translateAnim + 300f, translateAnim + 300f)
                )

                // Рисуем прямоугольник с заливкой
                drawRect(
                    brush = brush,

                    // Выполните композицию исходного изображения поверх целевого, но только в области их перекрытия.
                    // По сути, это оператор SrcOver, однако канал непрозрачности результирующего изображения устанавливается равным каналу непрозрачности целевого изображения,
                    // а не формируется как комбинация каналов непрозрачности обоих изображений.
                    // Вариант, при котором целевое изображение накладывается поверх исходного, см. в описании оператора DstAtop.
                    blendMode = BlendMode.SrcAtop
                )
            },
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // КНОПКА 1: PNG Растр -> Неявный Intent (Системные часы/будильник)
        val openSystemSetup = {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, "Подъём из приложения!")
                putExtra(AlarmClock.EXTRA_HOUR, 8)
                putExtra(AlarmClock.EXTRA_MINUTES, 0)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            }
            safeStartIntent(context, intent)
        }

        // КНОПКА 2: Иконка (ImageVector Material) -> Явный Intent на запуск другого Activity
        val startActivity = {
            val intent = Intent(context, DetailResultActivity::class.java)
            context.startActivity(intent)
        }

        // КНОПКА 3: Вектор из SVG (VectorDrawable XML) -> Activity с возвратом результата
        val startActivityResult = {
            val intent = Intent(context, DetailResultActivity::class.java)
            resultLauncher.launch(intent)
        }

        // КНОПКА 4: Процедурный будильник на Canvas -> Запуск Service
        val startService: () -> Unit = {
            val serviceIntent = Intent(context, AlarmForegroundService::class.java)
            context.startService(serviceIntent)
        }

        // КНОПКА 5: Custom VectorPainter -> Стороннее приложение (ACTION_MAIN / APPS)
        val safeStart = {
            val launchIntent =
                context.packageManager.getLaunchIntentForPackage("com.google.android.calculator")
                    ?: Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR)
            safeStartIntent(context, launchIntent)
        }

        val rasterImage =
            @androidx.compose.runtime.Composable {
                Image(
                    painter = painterResource(id = R.drawable.alarm_raster),
                    contentDescription = "PNG Alarm",
                    modifier = Modifier.fillMaxSize()
                )
            }

        val iconImage =
            @androidx.compose.runtime.Composable {
                Icon(
                    imageVector = Icons.Filled.Alarm,
                    contentDescription = "Vector Icon Alarm",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize()
                )
            }

        val svgImage =
            @androidx.compose.runtime.Composable {
                Image(
                    painter = painterResource(id = R.drawable.ic_alarm_svg),
                    contentDescription = "SVG Alarm",
                    modifier = Modifier.fillMaxSize()
                )
            }


        AlarmCardButton(
            title = "1. PNG Bitmap: Системный будильник",
            onClick = openSystemSetup
        ) {
            rasterImage()
        }

        AlarmCardButton(
            title = "2. Material ImageVector: Явный Intent",
            onClick = startActivity
        ) {
            iconImage()
        }

        AlarmCardButton(
            title = "3. XML/SVG Vector: Activity Result",
            onClick = startActivityResult
        ) {
            svgImage()
        }

        AlarmCardButton(
            title = "4. Procedural Canvas: Запуск Сервиса",
            onClick = startService
        ) {
            // Процедурный будильник на Canvas
            ProceduralCanvasAlarm(modifier = Modifier.fillMaxSize())
        }

        AlarmCardButton(
            title = "5. VectorPainter: Открытие приложения Калькулятор/Стороннего",
            onClick = safeStart
        ) {
            val customPainter = rememberVectorPainter(Icons.Outlined.AlarmOn)
            Image(
                painter = customPainter,
                contentDescription = "Painter Alarm",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}