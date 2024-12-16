package com.example.travelapp.app

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class TripNotificationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val tripId = inputData.getString("tripId")
        val city = inputData.getString("city") ?: "Unknown City"
        val country = inputData.getString("country") ?: "Unknown Country"
        val notificationId = inputData.getInt("notificationId", 0)

        if (tripId.isNullOrBlank()) {
            Log.e("TripNotificationWorker", "tripId is null or blank. Cannot proceed.")
            return Result.failure()
        }

        val title = "Upcoming Trip Reminder"
        val message = "Don't forget that your trip to $city, $country starts tomorrow. Press here to see more details."

        return try {
            NotificationUtils.showNotification(applicationContext, tripId, title, message, notificationId)
            Log.d("TripNotificationWorker", "Notification sent successfully for tripId: $tripId")
            Result.success()
        } catch (e: Exception) {
            Log.e("TripNotificationWorker", "Failed to send notification: ${e.message}")
            Result.failure()
        }
    }
}