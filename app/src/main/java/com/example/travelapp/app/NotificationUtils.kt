package com.example.travelapp.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.travelapp.MainActivity
import com.example.travelapp.R

object NotificationUtils {
    private const val CHANNEL_ID = "travel_app_channel"
    private const val CHANNEL_NAME = "Travel Notifications"
    private const val CHANNEL_DESC = "Notifications for trips and events"

    fun showNotification(
        context: Context,
        tripId: String,
        title: String,
        message: String,
        notificationId: Int
    ) {
        val notificationManager = NotificationManagerCompat.from(context)

        // Sprawdzanie uprawnień do powiadomień (Android 13 i nowsze)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        // Tworzenie kanału powiadomień (Android O i nowsze)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = CHANNEL_DESC }

            notificationManager.createNotificationChannel(channel)
        }

        // Tworzenie Intentu do otwarcia ekranu szczegółów podróży
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("tripId", tripId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Budowanie powiadomienia
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ikona aplikacji
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent) // Powiązanie z PendingIntent
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // Wyświetlanie powiadomienia
        notificationManager.notify(notificationId, notification)
    }
}