Стартовый промпт:
Создай учебное Android приложение, которое выводит на экран 5 изображений в колонку. Для построения 1 изображения используется png файл, второго изображения - иконка, третьего - векторное изображение, полученное из svg,  четвёртое - приложение нарисованное на Canvas. Все изображения представляют собой большие, как можно больше, кнопки, при нажатии который открывается другое приложение, запускается сервис или активити - в зависимости от кнопки. Все изображения представляют собой будильник. Изображение, рисуемое на Canvas, должно вызывать функции рисования дуг, гладких кривых, прямых, текста, использовать заливку. Таким образом мы демонстрируем, как можно выводить изображение, используя Compose. Кроме того, при старте приложения поверх кнопок прибегает блик - мы демонстрируем, как наложением маски мы можем управлять прозрачностью и тоном изображения, как организовать движущуюся маску. И, как говорилось, приложение демонстрирует использование намерений для вызова другого Активити, получения результата другого Активити, вызова другого приложения, запуска сервиса.

Приложение реализует:
1.	5 вариантов рендеринга графики: PNG растр, векторный ImageVector (Material Icons), импортированный XML Vector Drawable (из SVG), процедурный рендеринг на Canvas (дуги, Безье, прямые, текст, градиентная заливка), и кастомная векторная фигура через rememberVectorPainter.
2.	Анимацию пробегающего блика (Shimmer/Sheen mask): через кастомный Modifier.graphicsLayer в режиме наложения BlendMode.SrcAtop, что позволяет светящейся полосе скользить строго по контурам и телам самих кнопок.
3.	Разнообразные Android Intents: явный интент (Explicit Intent), неявный системный интент (Implicit Intent — открытие часов системы), запуск активности с получением результата (ActivityResultContracts), запуск фонового сервиса (startService), а также системный Action будильника (AlarmClock.ACTION_SET_ALARM). 

### Манифест (AndroidManifest.xml)
   Для запуска стандартного будильника и объявления сервиса с вспомогательным Activity добавляем соответствующие разрешения и компоненты:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Разрешение для установки системных будильников через Intent -->
    <uses-permission android:name="com.android.alarm.permission.SET_ALARM" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="Alarm Showcase"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Вспомогательное Activity для явного вызова и возврата результата -->
        <activity
            android:name=".DetailResultActivity"
            android:exported="false" />

        <!-- Демонстрационный сервис -->
        <service
            android:name=".AlarmForegroundService"
            android:exported="false" />

    </application>
</manifest>
```

### Ресурсы
(Для первого изображения положите любой PNG-файл будильника в res/drawable/alarm_raster.png).

#### SVG-вектор будильника (res/drawable/ic_alarm_svg.xml)
Файл сгенерирован конвертером из SVG и представляет третье изображение:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="128dp"
    android:height="128dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <!-- Ножки -->
    <path
        android:fillColor="#FF37474F"
        android:pathData="M5,20.5 L2.5,23 M19,20.5 L21.5,23"
        android:strokeWidth="2.2"
        android:strokeColor="#FF37474F"
        android:strokeLineCap="round"/>
    <!-- Звонки -->
    <path
        android:fillColor="#FFFFB300"
        android:pathData="M5.5,5.5 L3,8 C2,7 2,5 3.5,3.5 C5,2 7,2 8,3 L5.5,5.5 Z"/>
    <path
        android:fillColor="#FFFFB300"
        android:pathData="M18.5,5.5 L21,8 C22,7 22,5 20.5,3.5 C19,2 17,2 16,3 L18.5,5.5 Z"/>
    <!-- Корпус -->
    <path
        android:fillColor="#FFFFC107"
        android:pathData="M12,4 A8,8 0 1,0 12,20 A8,8 0 1,0 12,4 Z"/>
    <!-- Циферблат -->
    <path
        android:fillColor="#FFFFFFFF"
        android:pathData="M12,6 A6,6 0 1,0 12,18 A6,6 0 1,0 12,6 Z"/>
    <!-- Стрелки -->
    <path
        android:strokeColor="#FF263238"
        android:strokeWidth="1.8"
        android:strokeLineCap="round"
        android:pathData="M12,12 L12,8 M12,12 L15,14"/>
</vector>
```
#### Вспомогательные компоненты: Activity и Service
   DetailResultActivity.kt
   Возвращает результат вызывающей активности через setResult:

```kotlin
package com.example.alarmshowcase

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class DetailResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Экран детальной настройки будильника", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val returnIntent = Intent().apply {
                                    putExtra("EXTRA_RESULT_TIME", "07:30 AM — Успешно установлен")
                                }
                                setResult(Activity.RESULT_OK, returnIntent)
                                finish()
                            }
                        ) {
                            Text("Сохранить и вернуть результат")
                        }
                    }
                }
            }
        }
    }
}
```

#### AlarmForegroundService.kt
Демонстрационный сервис, подтверждающий запуск выводом лога/тоста:

```kotlin
package com.example.alarmshowcase

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast

class AlarmForegroundService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Фоновый сервис мониторинга тревог запущен!", Toast.LENGTH_SHORT).show()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
```
#### Основной код: UI, Canvas и Shimmer Mask (MainActivity.kt)
```kotlin
package com.example.alarmshowcase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.outlined.AlarmOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

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

/**
 * Оболочка кнопки: занимает максимально возможное пространство по ширине с фиксированной высотой.
 */
@Composable
fun AlarmCardButton(
    title: String,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
                content = content
            )
        }
    }
}

/**
 * Процедурный будильник, нарисованный на Canvas:
 * демонстрирует дуги, кубические кривые Безье, радиальные/линейные заливки, прямые и текст через nativeCanvas.
 */
@Composable
fun ProceduralCanvasAlarm(modifier: Modifier = Modifier) {
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

private fun safeStartIntent(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Компонент не найден: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
```

#### Архитектурные особенности реализации
•	Маска блика (Sheen): Реализована через BlendMode.SrcAtop в модификаторе всей Column с параметром CompositingStrategy.Offscreen. Это гарантирует аппаратное отсечение градиента исключительно по форме и пикселям кнопок, не заливая пустоты между ними.
•	Векторный пайплайн в Compose: Продемонстрирован переход от растра (painterResource) к стандартным декларативным ImageVector (Icon), внешним XML/SVG-векторам и низкоуровневому рисованию кривых Безье третьего порядка (Path.cubicTo) и дуг (drawArc).
•	Система Intents: Охватывает сценарии startActivity (явный), системный интент с Action и метаданными через Bundle extras, запуск через контракт обратного вызова ActivityResultContracts.StartActivityForResult, а также передачу сигнатуры в фоновый жизненный цикл службы (startService).