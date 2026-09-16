package kz.spatz.pictureswithintents


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
