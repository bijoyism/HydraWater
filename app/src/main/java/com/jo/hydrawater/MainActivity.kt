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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel(this)

        if (
            Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val prefs = getSharedPreferences("hydra", MODE_PRIVATE)

        val savedGoal = prefs.getInt("goal", 0)
        val savedIntake = prefs.getInt("intake", 0)
        val savedInterval = prefs.getInt("interval", 2)

        setContent {
            MaterialTheme {

                var age by remember { mutableStateOf("") }
                var height by remember { mutableStateOf("") }
                var weight by remember { mutableStateOf("") }

                var goal by remember { mutableStateOf(savedGoal) }
                var intake by remember { mutableStateOf(savedIntake) }
                var interval by remember { mutableStateOf(savedInterval) }

                var message by remember {
                    mutableStateOf(
                        if (savedGoal > 0)
                            "Your saved goal is $savedGoal ml/day."
                        else
                            "Enter your details to calculate your goal."
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            "Hydra",
                            style = MaterialTheme.typography.headlineLarge
                        )

                        Text("Your personal water reminder")

                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age (years)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("Height (cm)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {

                                val a = age.toDoubleOrNull()
                                val h = height.toDoubleOrNull()
                                val w = weight.toDoubleOrNull()

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
                                            .coerceIn(1000, 5000)

                                    goal = calculated
                                    intake = 0

                                    prefs.edit()
                                        .putInt("goal", goal)
                                        .putInt("intake", 0)
                                        .apply()

                                    message = "Daily goal: $goal ml"

                                } else {

                                    message =
                                        "Please enter valid age, height and weight."
                                }

                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Calculate Water Goal")
                        }

                        Text(message)

                        if (goal > 0) {

                            val progress =
                                (intake.toFloat() / goal.toFloat())
                                    .coerceIn(0f, 1f)

                            Text("Today: $intake / $goal ml")

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {

                                Button(
                                    onClick = {
                                        intake =
                                            (intake + 250).coerceAtMost(goal)

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
                                            (intake + 500).coerceAtMost(goal)

                                        prefs.edit()
                                            .putInt("intake", intake)
                                            .apply()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("+500 ml")
                                }
                            }
                        }

                        Text(
                            "Reminder interval: $interval hour" +
                                    if (interval == 1) "" else "s"
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
                                            .putInt("interval", hours)
                                            .apply()

                                        scheduleReminder(
                                            this@MainActivity,
                                            hours
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("${hours}h")
                                }
                            }
                        }

                        Text(
                            "Choose 1–4 hours for water reminders.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    companion object {

        private const val CHANNEL_ID = "hydra_reminders"

        fun createNotificationChannel(context: Context) {

            if (Build.VERSION.SDK_INT >= 26) {

                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Hydra reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                )

                context
                    .getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(channel)
            }
        }

        fun scheduleReminder(
            context: Context,
            hours: Int
        ) {

            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE)
                        as AlarmManager

            val intent =
                Intent(context, ReminderReceiver::class.java)

            val pending =
                PendingIntent.getBroadcast(
                    context,
                    1001,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )

            alarmManager.cancel(pending)

            val intervalMs =
                hours * 60L * 60L * 1000L

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + intervalMs,
                intervalMs,
                pending
            )
        }
    }
}
