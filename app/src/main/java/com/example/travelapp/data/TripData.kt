package com.example.travelapp.data

data class TripData(
    val city: String?,
    val country: String?,
    val startDate: Long?,
    val endDate: Long?,
    val baggageList: List<String>?,
    val notes: String?,
    val tickets: HashMap<String, String>?,
    val placeId: String?,
    val id: String?,
    val latitude: Double?,
    val longitude: Double?
    )

{
    constructor() : this(null, null, null, null, null, null, null, null, null, null, null)
}
