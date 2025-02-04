package ph.lyn.wagnermeter_testjob

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.lyn.wagnermeter_testjob.ui.theme.WagnerMeter_TestJobTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import android.util.Log.*
import android.content.SharedPreferences
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.google.firebase.Firebase
import com.google.firebase.database.database
import java.util.UUID

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        var showLoginDialogState = false
        
        if (isFirstLaunch()) {
            // Handle first launch logic here
            d("MainActivity", "App opened for the first time")
            showLoginDialogState = true
        } else {
            d("MainActivity", "App has been opened before")
        }

        enableEdgeToEdge()
        setContent {
            WagnerMeter_TestJobTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NumberGameScreen(
                        modifier = Modifier.padding(innerPadding),
                        initialShowLoginDialog = showLoginDialogState
                    )
                }
            }
        }
    }

    private fun isFirstLaunch(): Boolean {
        val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean("is_first_launch", true)
        if (isFirstLaunch) {
            sharedPreferences.edit().putBoolean("is_first_launch", false).apply()
        }
        return isFirstLaunch
    }
}

@Composable
fun NumberGameScreen(
    modifier: Modifier = Modifier,
    initialShowLoginDialog: Boolean = false
) {

    val context = LocalContext.current

    var currentNumber by remember { mutableIntStateOf(1) }
    var totalTaps by remember { mutableIntStateOf(0) }
    var targetNumber by remember { mutableIntStateOf((2..500).random()) }
    var showCongratulationsDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(initialShowLoginDialog) }
    var userID by remember { mutableStateOf("") }
    val username = getUsername(context)
    
    // Check if target is reached after each number change
    LaunchedEffect(currentNumber) {
        if (currentNumber == targetNumber) {
            showCongratulationsDialog = true
        }
    }

    fun logUserEvent(userId: String, eventName: String) {
        val database = Firebase.database
        val myRef = database.getReference(UUID.randomUUID().toString())

        val eventData = mapOf(
            "eventname" to eventName,
            "userid" to userId,
            "timestamp" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        )

        myRef.setValue(eventData)
            .addOnSuccessListener {
                d("Firebase", "Event logged successfully!")
            }
            .addOnFailureListener { error ->
                e("Firebase", "Error logging event: ${error.message}")
            }
    }

    fun logButtonClick(buttonColor: String) {
        logUserEvent(username, buttonColor)
    }

    // Add dialog
    if (showCongratulationsDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (totalTaps < 10) "Congratulations" else "Cool",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (totalTaps < 10) "You got the correct number in less than 10 taps!" else "You got the correct\nnumber in $totalTaps taps!",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            // Reset game
                            targetNumber = (2..500).random()
                            currentNumber = 1
                            totalTaps = 0
                            showCongratulationsDialog = false
                        }
                    ) {
                        Text("Start New Game")
                    }
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = Color(0xFF8c1946),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Get the Target Number Game",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Can you get this number?",
            fontSize = 18.sp
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Target:",
                fontSize = 20.sp,
                color = colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = targetNumber.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (targetNumber == currentNumber) Color(0xFF006400) else Color.Red
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "You got:",
                fontSize = 20.sp,
                color = colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = currentNumber.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Operation buttons
        Button(
            onClick = { 
                currentNumber += 1
                totalTaps++
                logButtonClick("Red")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 32.dp)
                .padding(vertical = 2.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000))
        ) {
            Text("+1", fontSize = 20.sp, color = Color.White)
        }

        Button(
            onClick = { 
                currentNumber -= 1
                totalTaps++
                logButtonClick("Orange")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 32.dp)
                .padding(vertical = 2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00))
        ) {
            Text("-1", fontSize = 20.sp)
        }

        Button(
            onClick = { 
                currentNumber *= 2
                totalTaps++
                logButtonClick("Yellow")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 32.dp)
                .padding(vertical = 2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFe6c200))
        ) {
            Text("×2", fontSize = 20.sp, color = Color.Black)
        }

        Button(
            onClick = { 
                currentNumber /= 2
                totalTaps++
                logButtonClick("Green")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 32.dp)
                .padding(vertical = 2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006400))
        ) {
            Text("÷2", fontSize = 20.sp)
        }

        Button(
            onClick = {
                currentNumber *= 10
                totalTaps++
                logButtonClick("Blue")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 32.dp)
                .padding(vertical = 2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00008B))
        ) {
            Text("x10", fontSize = 20.sp, color = Color.White)
        }

        Button(
            onClick = {
                currentNumber /= 5
                totalTaps++
                logButtonClick("Purple")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 32.dp)
                .padding(vertical = 2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082))
        ) {
            Text("÷5", fontSize = 20.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Total Taps:",
                fontSize = 20.sp,
                color = colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = totalTaps.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        TextButton(onClick = { showHistoryDialog = true 
        
        }) {
            Text("See Tap Logs", fontSize = 16.sp)
        }
    }

    if (showHistoryDialog) {

        Dialog(
            onDismissRequest = { showHistoryDialog = false }
        ) {
            
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Tap Logs for $username",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        HistoryView()
                        
                    }
                    TextButton(
                        onClick = { showHistoryDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { /* Do nothing on dismiss */ },
            title = { Text("Already a User?") },
            text = {
                Column {
                    Text("Enter Username")
                    TextField(
                        value = userID,
                        onValueChange = { 
                            userID = it
                        },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Handle "New User" button click
                    d("MainActivity", "User clicked New User")
                    if (userID.isBlank()) {
                        userID = generateUserID()
                    }
                    // save username to shared preferences
                    saveUsername(context, userID)
                    showLoginDialog = false // Close the dialog
                }) {
                    Text("START GAME")
                }
            }
        )
    }
}

fun generateUserID(): String {
    val letters = ('a'..'z') + ('A'..'Z') // Include both uppercase and lowercase letters
    val randomLetters = (1..3).map { letters.random() }.joinToString("")
    val randomDigits = (100..999).random() // Generate a random 3-digit number
    return randomLetters + randomDigits
}

fun saveUsername(context: Context, username: String) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("username", username)
    editor.apply() // Save changes asynchronously
    d("MainActivity", "Username: $username")
}

fun getUsername(context: Context): String {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    val username = sharedPreferences.getString("username", null) ?: generateUserID()
    if (!sharedPreferences.contains("username")) {
        sharedPreferences.edit().putString("username", username).apply()
    }
    return username
}

/*
fun getUsername(context: Context): String? {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    return sharedPreferences.getString("username", null) // Default value is null if not found
} */