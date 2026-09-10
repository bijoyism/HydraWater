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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

            var darkMode by remember {
                mutableStateOf(prefs.getBoolean("darkMode", false))
            }

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

            var remindersEnabled by remember {
                mutableStateOf(
                    prefs.getBoolean("remindersEnabled", false)
                )
            }

            var message by remember {
                mutableStateOf(
                    if (goal > 0)
                        "Stay hydrated throughout the day."
                    else
                        "Enter your details to calculate your daily goal."
                )
            }

            MaterialTheme(
                colorScheme =
                    if (darkMode)
                        androidx.compose.material3.darkColorScheme()
                    else
                        androidx.compose.material3.lightColorScheme()
            ) {

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(
                                rememberScrollState()
                            )
                            .padding(20.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(14.dp)
                    ) {

                        Text(
                            text = "Hydra",
                            style =
                                MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text =
                                "Your personal water reminder",
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        if (goal > 0) {

                            val progress =
                                (intake.toFloat() /
                                        goal.toFloat())
                                    .coerceIn(0f, 1f)

                            val remaining =
                                (goal - intake)
                                    .coerceAtLeast(0)

                            Card(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                shape =
                                    RoundedCornerShape(24.dp),
                                elevation =
                                    CardDefaults.cardElevation(
                                        defaultElevation = 4.dp
                                    )
                            ) {

                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally
                                ) {

                                    Text(
                                        "Today's hydration",
                                        style =
                                            MaterialTheme.typography
                                                .titleLarge,
                                        fontWeight =
                                            FontWeight.Bold
                                    )

                                    Spacer(
                                        Modifier.height(16.dp)
                                    )

                                    Box(
                                        modifier =
                                            Modifier
                                                .height(190.dp)
                                                .fillMaxWidth(),
                                        contentAlignment =
                                            Alignment.Center
                                    ) {

                                        CircularProgressIndicator(
                                            progress = {
                                                progress
                                            },
                                            modifier =
                                                Modifier
                                                    .fillMaxSize()
                                                    .padding(10.dp),
                                            strokeWidth = 14.dp
                                        )

                                        Column(
                                            horizontalAlignment =
                                                Alignment.CenterHorizontally
                                        ) {

                                            Text(
                                                "${(progress * 100)
                                                    .roundToInt()}%",
                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .headlineMedium,
                                                fontWeight =
                                                    FontWeight.Bold
                                            )

                                            Text(
                                                "$intake ml",
                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .titleMedium
                                            )
                                        }
                                    }

                                    Text(
                                        "$remaining ml remaining",
                                        style =
                                            MaterialTheme.typography
                                                .titleMedium
                                    )

                                    Spacer(
                                        Modifier.height(10.dp)
                                    )

                                    Text(
                                        "Daily goal: $goal ml",
                                        fontWeight =
                                            FontWeight.Medium
                                    )
                                }
                            }

                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {

                                Button(
                                    onClick = {
                                        intake =
                                            (intake + 250)
                                                .coerceAtMost(goal)

                                        prefs.edit()
                                            .putInt(
                                                "intake",
                                                intake
                                            )
                                            .apply()
                                    },
                                    modifier =
                                        Modifier.weight(1f)
                                ) {
                                    Text("+250 ml")
                                }

                                Button(
                                    onClick = {
                                        intake =
                                            (intake + 500)
                                                .coerceAtMost(goal)

                                        prefs.edit()
                                            .putInt(
                                                "intake",
                                                intake
                                            )
                                            .apply()
                                    },
                                    modifier =
                                        Modifier.weight(1f)
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
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {
                                Text("Reset today's intake")
                            }

                            Card(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                shape =
                                    RoundedCornerShape(20.dp)
                            ) {

                                Column(
                                    modifier =
                                        Modifier.padding(18.dp)
                                ) {

                                    Text(
                                        "Water reminders",
                                        style =
                                            MaterialTheme.typography
                                                .titleLarge,
                                        fontWeight =
                                            FontWeight.Bold
                                    )

                                    Spacer(
                                        Modifier.height(10.dp)
                                    )

                                    Row(
                                        modifier =
                                            Modifier.fillMaxWidth(),
                                        verticalAlignment =
                                            Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.SpaceBetween
                                    ) {

                                        Column(
                                            modifier =
                                                Modifier.weight(1f)
                                        ) {

                                            Text(
                                                if (remindersEnabled)
                                                    "Reminders ON"
                                                else
                                                    "Reminders OFF",
                                                fontWeight =
                                                    FontWeight.Medium
                                            )

                                            Text(
                                                "Every $interval hour" +
                                                    if (interval == 1)
                                                        ""
                                                    else
                                                        "s",
                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .bodySmall
                                            )
                                        }

                                        Switch(
                                            checked =
                                                remindersEnabled,
                                            onCheckedChange = { enabled ->

                                                remindersEnabled =
                                                    enabled

                                                prefs.edit()
                                                    .putBoolean(
                                                        "remindersEnabled",
                                                        enabled
                                                    )
                                                    .apply()

                                                if (enabled) {
                                                    scheduleReminder(
                                                        this@MainActivity,
                                                        interval
                                                    )
                                                } else {
                                                    cancelReminder(
                                                        this@MainActivity
                                                    )
                                                }
                                            }
                                        )
                                    }

                                    Spacer(
                                        Modifier.height(12.dp)
                                    )

                                    Text(
                                        "Reminder interval"
                                    )

                                    Spacer(
                                        Modifier.height(8.dp)
                                    )

                                    Row(
                                        modifier =
                                            Modifier.fillMaxWidth(),
                                        horizontalArrangement =
                                            Arrangement.spacedBy(8.dp)
                                    ) {

                                        for (hours in 1..4) {

                                            OutlinedButton(
                                                onClick = {

                                                    interval =
                                                        hours

                                                    prefs.edit()
                                                        .putInt(
                                                            "interval",
                                                            hours
                                                        )
                                                        .apply()

                                                    if (remindersEnabled) {
                                                        scheduleReminder(
                                                            this@MainActivity,
                                                            hours
                                                        )
                                                    }
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
                                        "Android battery saving may delay reminders.",
                                        style =
                                            MaterialTheme.typography
                                                .bodySmall
                                    )
                                }
                            }
                        }

                        Card(
                            modifier =
                                Modifier.fillMaxWidth(),
                            shape =
                                RoundedCornerShape(20.dp)
                        ) {

                            Column(
                                modifier =
                                    Modifier.padding(18.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {

                                Text(
                                    "Your details",
                                    style =
                                        MaterialTheme.typography
                                            .titleLarge,
                                    fontWeight =
                                        FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = age,
                                    onValueChange = {
                                        age = it
                                    },
                                    label = {
                                        Text("Age")
                                    },
                                    suffix = {
                                        Text("years")
                                    },
                                    keyboardOptions =
                                        KeyboardOptions(
                                            keyboardType =
                                                KeyboardType.Number
                                        ),
                                    modifier =
                                        Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = height,
                                    onValueChange = {
                                        height = it
                                    },
                                    label = {
                                        Text("Height")
                                    },
                                    suffix = {
                                        Text("cm")
                                    },
                                    keyboardOptions =
                                        KeyboardOptions(
                                            keyboardType =
                                                KeyboardType.Number
                                        ),
                                    modifier =
                                        Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = weight,
                                    onValueChange = {
                                        weight = it
                                    },
                                    label = {
                                        Text("Weight")
                                    },
                                    suffix = {
                                        Text("kg")
                                    },
                                    keyboardOptions =
                                        KeyboardOptions(
                                            keyboardType =
                                                KeyboardType.Decimal
                                        ),
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
                                            a == null ||
                                            h == null ||
                                            w == null
                                        ) {

                                            message =
                                                "Please enter all three values."

                                        } else if (
                                            a !in 1.0..120.0
                                        ) {

                                            message =
                                                "Please enter an age between 1 and 120."

                                        } else if (
                                            h !in 50.0..250.0
                                        ) {

                                            message =
                                                "Please enter a height between 50 and 250 cm."

                                        } else if (
                                            w !in 10.0..300.0
                                        ) {

                                            message =
                                                "Please enter a weight between 10 and 300 kg."

                                        } else {

                                            /*
                                             * General wellness estimate.
                                             *
                                             * Base:
                                             * 30 ml per kg
                                             *
                                             * Height adjustment:
                                             * Higher height slightly increases
                                             * the estimate.
                                             *
                                             * Age adjustment:
                                             * Adults 65+ receive a small
                                             * reduction.
                                             */

                                            val base =
                                                w * 30.0

                                            val heightAdjustment =
                                                when {
                                                    h >= 180 -> 250
                                                    h >= 165 -> 150
                                                    else -> 0
                                                }

                                            val ageAdjustment =
                                                when {
                                                    a < 18 -> 0
                                                    a >= 65 -> -100
                                                    else -> 0
                                                }

                                            val calculated =
                                                (
                                                    base +
                                                        heightAdjustment +
                                                        ageAdjustment
                                                    )
                                                    .roundToInt()
                                                    .coerceIn(
                                                        1000,
                                                        5000
                                                    )

                                            goal =
                                                calculated

                                            intake =
                                                0

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
                                                "Your daily goal is $goal ml."
                                        }
                                    },
                                    modifier =
                                        Modifier.fillMaxWidth()
                                ) {
                                    Text("Update Water Goal")
                                }

                                Text(
                                    message,
                                    textAlign =
                                        TextAlign.Center,
                                    modifier =
                                        Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Card(
                            modifier =
                                Modifier.fillMaxWidth(),
                            shape =
                                RoundedCornerShape(20.dp)
                        ) {

                            Column(
                                modifier =
                                    Modifier.padding(18.dp)
                            ) {

                                Text(
                                    "Appearance",
                                    style =
                                        MaterialTheme.typography
                                            .titleLarge,
                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Spacer(
                                    Modifier.height(8.dp)
                                )

                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    RadioButton(
                                        selected =
                                            !darkMode,
                                        onClick = {

                                            darkMode =
                                                false

                                            prefs.edit()
                                                .putBoolean(
                                                    "darkMode",
                                                    false
                                                )
                                                .apply()
                                        }
                                    )

                                    Text("Light")
                                }

                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    RadioButton(
                                        selected =
                                            darkMode,
                                        onClick = {

                                            darkMode =
                                                true

                                            prefs.edit()
                                                .putBoolean(
                                                    "darkMode",
                                                    true
                                                )
                                                .apply()
                                        }
                                    )

                                    Text("Dark")
                                }

                                HorizontalDivider(
                                    modifier =
                                        Modifier.padding(
                                            vertical = 8.dp
                                        )
                                )

                                Text(
                                    "Hydra v1.2",
                                    style =
                                        MaterialTheme.typography
                                            .bodySmall
                                )

                                Text(
                                    "Drink water, stay hydrated.",
                                    style =
                                        MaterialTheme.typography
                                            .bodySmall
                                )
                            }
                        }

                        Spacer(
                            Modifier.height(4.dp)
                        )

                        Text(
                            "Hydra • Your personal water reminder",
                            style =
                                MaterialTheme.typography.bodySmall,
                            textAlign =
                                TextAlign.Center,
                            modifier =
                                Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    companion object {

        private const val CHANNEL_ID =
            "hydra_reminders"

        private const val REQUEST_CODE =
            1001

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
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
                )

            alarmManager.cancel(
                pendingIntent
            )

            val intervalMillis =
                hours * 60L * 60L * 1000L

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() +
                    intervalMillis,
                intervalMillis,
                pendingIntent
            )
        }

        private fun cancelReminder(
            context: Context
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
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
                )

            alarmManager.cancel(
                pendingIntent
            )
        }
    }
}