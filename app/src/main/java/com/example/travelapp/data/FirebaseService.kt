package com.example.travelapp.data
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseService {

    fun registerUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        // Zapisanie danych użytkownika w Firestore
                        saveUserData(
                            uid,
                            firstName,
                            lastName,
                            email,) { success, errorMessage ->
                                onComplete(success, errorMessage)
                        }
                    }
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Registration failed"
                    onComplete(false, errorMessage)
                }
            }
    }


    // Funkcja do zapisywania danych użytkownika w Firestore
    private fun saveUserData(
        uid: String,
        firstName: String,
        lastName: String,
        email: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val user = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
        )

        db.collection("users").document(uid)
            .set(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    val errorMessage =
                        task.exception?.localizedMessage ?: "Failed to save user data"
                    onComplete(false, errorMessage)
                }
            }
    }

    fun saveTrip(
        city: String,
        country: String,
        startDate: Long?,
        endDate: Long?,
        baggageList: List<String>,
        notes: String,
        latitude: Double?,
        longitude: Double?,
        tickets: HashMap<String, String>,
        placeID: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            onFailure(Exception("User not logged in"))
            return
        }
        val tripData = hashMapOf(
            "ownerId" to userId, "city" to city, "country" to country,
            "startDate" to startDate, "endDate" to endDate, "baggage" to baggageList,
            "notes" to notes, "tickets" to tickets, "placeId" to placeID,
            "sharedWith" to emptyList<String>(), "latitude" to latitude, "longitude" to longitude
        )
        val db = FirebaseFirestore.getInstance()

        db.collection("trips")
            .add(tripData)
            .addOnSuccessListener { documentReference ->
                val generatedId = documentReference.id
                documentReference.update("id", generatedId)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            }
    }

    fun fetchNotifications(
        onSuccess: (List<NotificationData>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        val db = FirebaseFirestore.getInstance()
        val notificationList = mutableListOf<NotificationData>()

        db.collection("notifications")
            .whereEqualTo("receiverId", userId)
            .get()
            .addOnSuccessListener { documents ->
                Log.d(
                    "FetchNotifications",
                    "Fetched ${documents.size()} notifications owned by user"
                )
                for (document in documents) {
                    val notify = NotificationData(
                        id = document.id,
                        receiverId = document.getString("receiverId") ?: "",
                        senderName = document.getString("senderName") ?: "",
                        tripId = document.getString("tripId") ?: "",
                        city = document.getString("city") ?: "",
                        country = document.getString("country") ?: "",
                        startDate = document.getLong("startDate"),
                        endDate = document.getLong("endDate"),
                        status = document.getString("status") ?: "",
                    )
                    notificationList.add(notify)
                }
                onSuccess(notificationList)
            }
    }

    fun fetchTrips(
        onSuccess: (List<TripData>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        val db = FirebaseFirestore.getInstance()
        val tripsList = mutableListOf<TripData>()

        // Pobieranie podróży, których właścicielem jest użytkownik
        db.collection("trips")
            .whereEqualTo("ownerId", userId)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("FetchTrips", "Fetched ${documents.size()} trips owned by user.")
                for (document in documents) {
                    val trip = TripData(
                        city = document.getString("city") ?: "",
                        country = document.getString("country") ?: "",
                        startDate = document.getLong("startDate"),
                        endDate = document.getLong("endDate"),
                        baggageList = document.get("baggage") as? List<String> ?: emptyList(),
                        notes = document.getString("notes") ?: "",
                        tickets = document.get("tickets") as? HashMap<String, String> ?: hashMapOf(),
                        placeId = document.getString("placeId") ?: "",
                        id = document.id,
                        longitude = document.getDouble("longitude"),
                        latitude = document.getDouble("latitude")
                    )
                    tripsList.add(trip)
                }


                // Pobieranie podróży, które są udostępnione użytkownikowi
                db.collection("trips")
                    .whereArrayContains("sharedWith", userId)
                    .get()
                    .addOnSuccessListener { sharedDocuments ->
                        Log.d("FetchTrips", "Fetched ${sharedDocuments.size()} shared trips.")
                        for (document in sharedDocuments) {
                            val sharedTrip = TripData(
                                city = document.getString("city") ?: "",
                                country = document.getString("country") ?: "",
                                startDate = document.getLong("startDate"),
                                endDate = document.getLong("endDate"),
                                baggageList = document.get("baggage") as? List<String> ?: emptyList(),
                                notes = document.getString("notes") ?: "",
                                tickets = document.get("tickets") as? HashMap<String, String> ?: hashMapOf(),
                                placeId = document.getString("placeId") ?: "",
                                id = document.id,
                                longitude = document.getDouble("longitude"),
                                latitude = document.getDouble("latitude")
                            )
                            tripsList.add(sharedTrip)
                        }

                        val sortedTrips = tripsList.sortedBy { it.startDate ?: Long.MAX_VALUE }

                        // Zwróć wszystkie podróże (zarówno własne, jak i udostępnione)
                        Log.d("FetchTrips", "Total trips fetched: ${tripsList.size}")
                        onSuccess(sortedTrips)
                    }
                    .addOnFailureListener { exception ->
                        Log.w("FetchTrips", "Error fetching shared trips", exception)
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.w("FetchTrips", "Error fetching user-owned trips", exception)
                onFailure(exception)
            }
    }



    // zapisanie powiadomienia w bazie danych
    fun sendNotification(
        receiverId: String,
        senderName: String,
        tripId: String,
        city: String,
        country: String,
        startDate: Long,
        endDate: Long,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val notificationData = hashMapOf(
            "receiverId" to receiverId,
            "senderName" to senderName,
            "tripId" to tripId,
            "city" to city,
            "country" to country,
            "startDate" to startDate,
            "endDate" to endDate,
            "status" to "pending"
        )

        db.collection("notifications")
            .add(notificationData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}


fun saveTaskToFirestore(tripId: String, day: Int, task: Task, context: Context) {
    val db = FirebaseFirestore.getInstance()
    val tripRef = db.collection("trips").document(tripId)

    tripRef.get().addOnSuccessListener { document ->
        val planner = document.get("planner") as? MutableList<Map<String, Any>> ?: mutableListOf()
        val dayEntry = planner.find { it["day"] == day } as? MutableMap<String, Any>
        if (dayEntry != null) {
            val tasks = dayEntry["tasks"] as? MutableList<Map<String, String>> ?: mutableListOf()
            tasks.add(
                mapOf(
                    "timeStart" to task.startHour,
                    "timeEnd" to task.endHour,
                    "activity" to task.content
                )
            )
            dayEntry["tasks"] = tasks
        } else {
            planner.add(
                mapOf(
                    "day" to day,
                    "tasks" to listOf(
                        mapOf(
                            "timeStart" to task.startHour,
                            "timeEnd" to task.endHour,
                            "activity" to task.content
                        )
                    )
                )
            )
        }
        tripRef.update("planner", planner)
    }
}


fun loadTasksFromFirestore(
    tripId: String,
    tasksByDay: MutableState<MutableMap<Int, MutableList<Task>>>
) {
    val db = FirebaseFirestore.getInstance()
    val tripRef = db.collection("trips").document(tripId)

    tripRef.get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val planner = document.get("planner") as? List<Map<String, Any>>
                val newTasksByDay = mutableMapOf<Int, MutableList<Task>>()

                planner?.forEach { dayEntry ->
                    val day = (dayEntry["day"] as? Long)?.toInt() ?: return@forEach
                    val tasks = (dayEntry["tasks"] as? List<Map<String, String>>)?.mapNotNull { taskMap ->
                        val startHour = taskMap["timeStart"]
                        val endHour = taskMap["timeEnd"]
                        val content = taskMap["activity"]
                        if (startHour != null && endHour != null && content != null) {
                            Task(startHour, endHour, content)
                        } else null
                    } ?: emptyList()

                    // Dodajemy wydarzenia do istniejącej listy lub tworzymy nową
                    if (tasks.isNotEmpty()) {
                        val existingTasks = newTasksByDay[day] ?: mutableListOf()
                        existingTasks.addAll(tasks)
                        newTasksByDay[day] = existingTasks
                    }
                }

                // Aktualizacja całej mapy
                tasksByDay.value = newTasksByDay

                // Logowanie wczytanych danych
                newTasksByDay.forEach { (day, tasks) ->
                    val tasksDescriptions = tasks.joinToString(", ") { "${it.startHour}-${it.endHour}: ${it.content}" }
                    Log.d("Firestore", "Dzień $day: $tasksDescriptions")
                }
            } else {
                Log.e("Firestore", "Document does not exist.")
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error fetching tasks: ${e.message}")
        }
}


fun deleteTaskFromFirestore(tripId: String, day: Int, task: Task, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val tripRef = db.collection("trips").document(tripId)

    tripRef.get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val planner = document.get("planner") as? MutableList<Map<String, Any>>
                if (planner != null) {
                    // Znajdź dzień i usuń zadanie
                    val updatedPlanner = planner.mapNotNull { dayEntry ->
                        val dayId = (dayEntry["day"] as? Long)?.toInt()
                        val tasks = (dayEntry["tasks"] as? MutableList<Map<String, String>>)?.toMutableList()
                        if (dayId == day) {
                            tasks?.removeIf {
                                it["timeStart"] == task.startHour &&
                                        it["timeEnd"] == task.endHour &&
                                        it["activity"] == task.content
                            }
                        }
                        if (tasks != null) mapOf("day" to dayId!!, "tasks" to tasks) else null
                    }

                    // Zapisz zaktualizowaną listę w bazie danych
                    tripRef.update("planner", updatedPlanner)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onFailure(e) }
                }
            }
        }
        .addOnFailureListener { e -> onFailure(e) }
}