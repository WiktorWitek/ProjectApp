package com.example.travelapp.data

data class NotificationData(
    val id: String?,
    val receiverId: String?,
    val senderName: String?,
    val tripId: String?,
    val city: String?,
    val country: String?,
    val startDate: Long?,
    val endDate: Long?,
    val status: String?,
) {
    constructor() : this(null, null, null, null, null, null, null, null, "pending")
}
