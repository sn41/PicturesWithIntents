package kz.spatz.pictureswithintents


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
