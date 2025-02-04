package ph.lyn.wagnermeter_testjob

import android.util.Log.e
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.CompletableDeferred
import android.util.Log.d
import android.util.Log.w
import kotlinx.serialization.Serializable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Serializable
data class EventItem(
    val eventName: String,
    val timestamp: String
)

fun getColor(buttonName: String): Color {
    return when (buttonName) {
        "Red" -> Color.Red
        "Orange" -> Color(0xFFFFA500)
        "Yellow" -> Color(0xFFe6c200)
        "Green" -> Color.Green
        "Blue" -> Color.Blue
        "Purple" -> Color(0xFF800080) // Standard purple color
        else -> Color.DarkGray
    }
}


@Composable
fun LogEntry(buttonColor: String, timestamp: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "$buttonColor button pressed",
            fontSize = 18.sp,
            color = getColor(buttonColor)
        )
        Text(
            text = "Time: $timestamp",
            fontSize = 14.sp
        )
    }
}

suspend fun fetchFirebaseData(username: String): DataSnapshot? {
    val database = Firebase.database
    val rootRef = database.reference
    val result = CompletableDeferred<DataSnapshot?>()

    rootRef.orderByChild("userid").equalTo(username)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    result.complete(dataSnapshot)
                } else {
                    result.complete(null)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                e("Firebase", "Error fetching data: ${databaseError.message}")
                result.completeExceptionally(databaseError.toException())
            }
        })

    return result.await()
}

@Composable
fun HistoryView() {
    var eventItems by remember { mutableStateOf<List<EventItem>>(emptyList()) }
    val context = LocalContext.current
    val username = getUsername(context)
    var data by remember { mutableStateOf<DataSnapshot?>(null) }

    d("HistoryView", "Username: $username")

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        item {
            LaunchedEffect(username) {
                if (username != null) {
                    try {
                        data = fetchFirebaseData(username)
                        d("HistoryView", "Data fetched: $data")
                    } catch (e: Exception) {
                        e("HistoryView", "Error fetching data: ${e.message}")
                    }
                } else {
                    w("HistoryView", "Username is null")
                }

                data?.let { snapshot ->
                    val fetchedEventItems = mutableListOf<EventItem>()
                    for (item in snapshot.children) {
                        val eventData = item.value as? Map<*, *>
                        val eventName = eventData?.get("eventname") as? String
                        val timestamp = eventData?.get("timestamp") as? String
                        if (eventName != null && timestamp != null) {
                            fetchedEventItems.add(EventItem(eventName, timestamp))
                        } else {
                            w("HistoryView", "Invalid event data: $eventData")
                        }
                    }
                    eventItems = fetchedEventItems.sortedBy { it.timestamp }
                    d("HistoryView", "Event items: $eventItems")
                } ?: w("HistoryView", "Data is null")
            }
        }

        items(eventItems) { event ->
            LogEntry(
                buttonColor = event.eventName,
                timestamp = event.timestamp
            )
        }
    }
}