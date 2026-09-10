package com.jo.hydrawater

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel(this)

        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        val prefs = getSharedPreferences("hydra", MODE_PRIVATE)

        val today = java.time.LocalDate.now().toString()
        val savedDate = prefs.getString("date", "")

        if (savedDate != today) {
            prefs.edit()
                .putInt("intake", 0)
                .putString("date", today)
                .apply()
        }

        setContent {

            var age by remember {
                mutableStateOf(prefs.getString("age", "") ?: "")
            }

            var height by remember {
                mutableStateOf(prefs.getString("height", "") ?: "")
            }

            var weight by remember {
                mutableStateOf(prefs.getString("weight", "") ?: "")
            }

            var goal by remember {
                mutableStateOf(prefs.getInt("goal", 0))
            }

            var intake by remember {
                mutableStateOf(prefs.getInt("intake", 0))
            }

            var interval by remember {
                mutableStateOf(prefs.getInt("interval", 2))
            }

            var message by remember {
                mutableStateOf(
                    if (goal > 0)
                        "Stay hydrated throughout the day."
                    else
                        "Enter your details to calculate your daily goal."
                )
            }

            MaterialTheme {

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        Text(
                            text = "Hydra",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Your personal water reminder",
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (goal > 0) {

                            val progress =
                                (intake.toFloat() / goal.toFloat())
                                    .coerceIn(0f, 1f)

                            val remaining =
                                (goal - intake).coerceAtLeast(0)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 4.dp
                                )
                            ) {

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally
                                ) {

                                    Text(
                                        "Today's hydration",
                                        style =
                                            MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(
                                        Modifier.height(16.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .height(190.dp)
                                            .fillMaxWidth(),
                                        contentAlignment =
                                            Alignment.Center
                                    ) {

                                        CircularProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(10.dp),
                                            strokeWidth = 14.dp
                                        )

                                        Column(
                                            horizontalAlignment =
                                                Alignment.CenterHorizontally
                                        ) {

                                            Text(
                                                "${(progress * 100).roundToInt()}%",
                                                style = MaterialTheme
                                                    .typography
                                                    .headlineMedium,
                                                fontWeight =
                                                    FontWeight.Bold
                                            )

                                            Text(
                                                "$intake ml",
                                                style =
                                                    MaterialTheme.typography
                                                        .titleMedium
                                            )
                                        }
                                    }

                                    Text(
                                        "$remaining ml remaining",
                                        style =
                                            MaterialTheme.typography.titleMedium
                                    )

                                    Spacer(
                                        Modifier.height(14.dp)
                                    )

                                    Text(
                                        "Daily goal: $goal ml",
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {

                                Button(
                                    onClick = {
                                        intake =
                                            (intake + 250)
                                                .coerceAtMost(goal)

                                        prefs.edit()
                                            .putInt("intake", intake)
                                            .apply()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("+250 ml")
                                }

                                Button(
                                    onClick = {
                                        intake =
                                            (intake + 500)
                                                .coerceAtMost(goal)

                                        prefs.edit()
                                            .putInt("intake", intake)
                                            .apply()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("+500 ml")
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    intake = 0
                                    prefs.edit()
                                        .putInt("intake", 0)
                                        .apply()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reset today's intake")
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp)
                            ) {

                                Column(
                                    modifier = Modifier.padding(18.dp)
                                ) {

                                    Text(
                                        "Water reminders",
                                        style =
                                            MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(
                                        Modifier.height(8.dp)
                                    )

                                    Text(
                                        "Remind me every $interval hour" +
                                            if (interval == 1)
                                                ""
                                            else
                                                "s"
                                    )

                                    Spacer(
                                        Modifier.height(12.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement =
                                            Arrangement.spacedBy(8.dp)
                                    ) {

                                        for (hours in 1..4) {

                                            Button(
                                                onClick = {

                                                    interval = hours

                                                    prefs.edit()
                                                        .putInt(
                                                            "interval",
                                                            hours
                                                        )
                                                        .apply()

                                                    scheduleReminder(
                                                        this@MainActivity,
                                                        hours
                                                    )
                                                },
                                                modifier =
                                                    Modifier.weight(1f)
                                            ) {
                                                Text("${hours}h")
                                            }
                                        }
                                    }

                                    Spacer(
                                        Modifier.height(8.dp)
                                    )

                                    Text(
                                        "Reminders may be delayed by Android battery-saving features.",
                                        style =
                                            MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp)
                        ) {

                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {

                                Text(
                                    "Your details",
                                    style =
                                        MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = age,
                                    onValueChange = {
                                        age = it
                                    },
                                    label = {
                                        Text("Age (years)")
                                    },
                                    modifier =
                                        Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = height,
                                    onValueChange = {
                                        height = it
                                    },
                                    label = {
                                        Text("Height (cm)")
                                    },
                                    modifier =
                                        Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = weight,
                                    onValueChange = {
                                        weight = it
                                    },
                                    label = {
                                        Text("Weight (kg)")
                                    },
                                    modifier =
                                        Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {

                                        val a =
                                            age.toDoubleOrNull()

                                        val h =
                                            height.toDoubleOrNull()

                                        val w =
                                            weight.toDoubleOrNull()

                                        if (
                                            a != null &&
                                            h != null &&
                                            w != null &&
                                            a > 0 &&
                                            h > 0 &&
                                            w > 0
                                        ) {

                                            val ageFactor =
                                                when {
                                                    a < 18 -> 1.0
                                                    a >= 65 -> 0.95
                                                    else -> 1.0
                                                }

                                            val calculated =
                                                (w * 35.0 * ageFactor)
                                                    .roundToInt()
                                                    .coerceIn(
                                                        1000,
                                                        5000
                                                    )

                                            goal = calculated
                                            intake = 0

                                            prefs.edit()
                                                .putString(
                                                    "age",
                                                    age
                                                )
                                                .putString(
                                                    "height",
                                                    height
                                                )
                                                .putString(
                                                    "weight",
                                                    weight
                                                )
                                                .putInt(
                                                    "goal",
                                                    goal
                                                )
                                                .putInt(
                                                    "intake",
                                                    0
                                                )
                                                .putString(
                                                    "date",
                                                    today
                                                )
                                                .apply()

                                            message =
                                                "Your new daily goal is $goal ml."

                                        } else {

                                            message =
                                                "Please enter valid age, height and weight."
                                        }
                                    },
                                    modifier =
                                        Modifier.fillMaxWidth()
                                ) {
                                    Text("Update Water Goal")
                                }

                                Text(
                                    message,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Text(
                            "Hydra • Drink water, stay hydrated.",
                            style =
                                MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    companion object {

        private const val CHANNEL_ID =
            "hydra_reminders"

        private fun createNotificationChannel(
            context: Context
        ) {

            if (Build.VERSION.SDK_INT >= 26) {

                val channel =
                    NotificationChannel(
                        CHANNEL_ID,
                        "Hydra water reminders",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )

                val manager =
                    context.getSystemService(
                        NotificationManager::class.java
                    )

                manager.createNotificationChannel(channel)
            }
        }

        private fun scheduleReminder(
            context: Context,
            hours: Int
        ) {

            val alarmManager =
                context.getSystemService(
                    Context.ALARM_SERVICE
                ) as AlarmManager

            val intent =
                Intent(
                    context,
                    ReminderReceiver::class.java
                )

            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    1001,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
                )

            alarmManager.cancel(pendingIntent)

            val intervalMillis =
                hours * 60L * 60L * 1000L

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + intervalMillis,
                intervalMillis,
                pendingIntent
            )
        }
    }
}