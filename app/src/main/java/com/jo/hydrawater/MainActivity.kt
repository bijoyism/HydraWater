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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

        if (prefs.getString("date", "") != today) {
            prefs.edit()
                .putInt("intake", 0)
                .putString("date", today)
                .apply()
        }

        setContent {

            var darkMode by remember {
                mutableStateOf(
                    prefs.getBoolean("darkMode", false)
                )
            }

            var age by remember {
                mutableStateOf(
                    prefs.getString("age", "") ?: ""
                )
            }

            var height by remember {
                mutableStateOf(
                    prefs.getString("height", "") ?: ""
                )
            }

            var weight by remember {
                mutableStateOf(
                    prefs.getString("weight", "") ?: ""
                )
            }

            var goal by remember {
                mutableStateOf(
                    prefs.getInt("goal", 0)
                )
            }

            var intake by remember {
                mutableStateOf(
                    prefs.getInt("intake", 0)
                )
            }

            var reminderMinutes by remember {
                mutableStateOf(
                    prefs.getInt("reminderMinutes", 60)
                )
            }

            var remindersEnabled by remember {
                mutableStateOf(
                    prefs.getBoolean(
                        "remindersEnabled",
                        false
                    )
                )
            }

            var customAmount by remember {
                mutableStateOf("")
            }

            var customReminder by remember {
                mutableStateOf("")
            }

            var message by remember {
                mutableStateOf(
                    if (goal > 0)
                        "Stay hydrated throughout the day."
                    else
                        "Enter your details to calculate your goal."
                )
            }

            MaterialTheme(
                colorScheme =
                    if (darkMode)
                        darkColorScheme()
                    else
                        lightColorScheme()
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
                            "Hydra",
                            style =
                                MaterialTheme.typography
                                    .displaySmall,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            "Your personal water reminder",
                            style =
                                MaterialTheme.typography
                                    .titleMedium
                        )

                        if (goal > 0) {

                            val targetProgress =
                                (intake.toFloat() /
                                    goal.toFloat())
                                    .coerceIn(0f, 1f)

                            val animatedProgress by
                                animateFloatAsState(
                                    targetValue =
                                        targetProgress,
                                    label = "waterLevel"
                                )

                            val remaining =
                                (goal - intake)
                                    .coerceAtLeast(0)

                            Card(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                shape =
                                    RoundedCornerShape(24.dp)
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
                                            MaterialTheme
                                                .typography
                                                .titleLarge,
                                        fontWeight =
                                            FontWeight.Bold
                                    )

                                    Spacer(
                                        Modifier.height(16.dp)
                                    )

                                    /*
                                     * Animated glass.
                                     * The inner water container grows
                                     * according to the daily progress.
                                     */
                                    Box(
                                        modifier =
                                            Modifier
                                                .width(150.dp)
                                                .height(210.dp)
                                                .border(
                                                    BorderStroke(
                                                        4.dp,
                                                        MaterialTheme
                                                            .colorScheme
                                                            .primary
                                                    ),
                                                    RoundedCornerShape(
                                                        28.dp
                                                    )
                                                )
                                                .clip(
                                                    RoundedCornerShape(
                                                        28.dp
                                                    )
                                                ),
                                        contentAlignment =
                                            Alignment.BottomCenter
                                    ) {

                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(
                                                        animatedProgress
                                                    )
                                                    .clip(
                                                        RoundedCornerShape(
                                                            topStart = 20.dp,
                                                            topEnd = 20.dp
                                                        )
                                                    )
                                        )

                                        Column(
                                            horizontalAlignment =
                                                Alignment.CenterHorizontally
                                        ) {

                                            Text(
                                                "${(
                                                    targetProgress * 100
                                                ).roundToInt()}%",
                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .headlineMedium,
                                                fontWeight =
                                                    FontWeight.Bold
                                            )

                                            Text(
                                                "$intake ml",
                                                fontWeight =
                                                    FontWeight.Medium
                                            )
                                        }
                                    }

                                    Spacer(
                                        Modifier.height(14.dp)
                                    )

                                    Text(
                                        "$remaining ml remaining",
                                        style =
                                            MaterialTheme
                                                .typography
                                                .titleMedium
                                    )

                                    Text(
                                        "Daily goal: $goal ml"
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
                                            (
                                                intake + 250
                                            ).coerceAtMost(goal)

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
                                            (
                                                intake + 500
                                            ).coerceAtMost(goal)

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
                                        "Custom water amount",
                                        style =
                                            MaterialTheme
                                                .typography
                                                .titleLarge,
                                        fontWeight =
                                            FontWeight.Bold
                                    )

                                    OutlinedTextField(
                                        value =
                                            customAmount,
                                        onValueChange = {
                                            customAmount = it
                                        },
                                        label = {
                                            Text(
                                                "Amount in ml"
                                            )
                                        },
                                        keyboardOptions =
                                            KeyboardOptions(
                                                keyboardType =
                                                    KeyboardType.Number
                                            ),
                                        modifier =
                                            Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {

                                            val amount =
                                                customAmount
                                                    .toIntOrNull()

                                            if (
                                                amount != null &&
                                                amount > 0
                                            ) {

                                                intake =
                                                    (
                                                        intake +
                                                            amount
                                                    ).coerceAtMost(
                                                        goal
                                                    )

                                                prefs.edit()
                                                    .putInt(
                                                        "intake",
                                                        intake
                                                    )
                                                    .apply()

                                                customAmount = ""

                                            } else {

                                                message =
                                                    "Enter a valid water amount."
                                            }
                                        },
                                        modifier =
                                            Modifier.fillMaxWidth()
                                    ) {
                                        Text("Add Water")
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = {

                                    intake = 0

                                    prefs.edit()
                                        .putInt(
                                            "intake",
                                            0
                                        )
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
                                        Modifier.padding(18.dp),
                                    verticalArrangement =
                                        Arrangement.spacedBy(10.dp)
                                ) {

                                    Text(
                                        "Water reminders",
                                        style =
                                            MaterialTheme
                                                .typography
                                                .titleLarge,
                                        fontWeight =
                                            FontWeight.Bold
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
                                                if (
                                                    remindersEnabled
                                                )
                                                    "Reminders ON"
                                                else
                                                    "Reminders OFF",
                                                fontWeight =
                                                    FontWeight.Bold
                                            )

                                            Text(
                                                reminderText(
                                                    reminderMinutes
                                                ),
                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .bodySmall
                                            )
                                        }

                                        Switch(
                                            checked =
                                                remindersEnabled,
                                            onCheckedChange = {
                                                enabled ->

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
                                                        reminderMinutes
                                                    )

                                                } else {

                                                    cancelReminder(
                                                        this@MainActivity
                                                    )
                                                }
                                            }
                                        )
                                    }

                                    Text(
                                        "Quick intervals"
                                    )

                                    val quickIntervals =
                                        listOf(
                                            10,
                                            15,
                                            20,
                                            30,
                                            45,
                                            60,
                                            120,
                                            180,
                                            240
                                        )

                                    quickIntervals
                                        .chunked(3)
                                        .forEach { row ->

                                            Row(
                                                modifier =
                                                    Modifier.fillMaxWidth(),
                                                horizontalArrangement =
                                                    Arrangement.spacedBy(
                                                        8.dp
                                                    )
                                            ) {

                                                row.forEach { minutes ->

                                                    OutlinedButton(
                                                        onClick = {

                                                            reminderMinutes =
                                                                minutes

                                                            prefs.edit()
                                                                .putInt(
                                                                    "reminderMinutes",
                                                                    minutes
                                                                )
                                                                .apply()

                                                            if (
                                                                remindersEnabled
                                                            ) {
                                                                scheduleReminder(
                                                                    this@MainActivity,
                                                                    minutes
                                                                )
                                                            }
                                                        },
                                                        modifier =
                                                            Modifier.weight(
                                                                1f
                                                            )
                                                    ) {
                                                        Text(
                                                            reminderShortText(
                                                                minutes
                                                            )
                                                        )
                                                    }
                                                }

                                                repeat(
                                                    3 - row.size
                                                ) {
                                                    Spacer(
                                                        Modifier.weight(
                                                            1f
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                    Spacer(
                                        Modifier.height(4.dp)
                                    )

                                    Text(
                                        "Custom interval"
                                    )

                                    OutlinedTextField(
                                        value =
                                            customReminder,
                                        onValueChange = {
                                            customReminder = it
                                        },
                                        label = {
                                            Text(
                                                "Minutes (minimum 10)"
                                            )
                                        },
                                        keyboardOptions =
                                            KeyboardOptions(
                                                keyboardType =
                                                    KeyboardType.Number
                                            ),
                                        modifier =
                                            Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {

                                            val minutes =
                                                customReminder
                                                    .toIntOrNull()

                                            if (
                                                minutes != null &&
                                                minutes >= 10
                                            ) {

                                                reminderMinutes =
                                                    minutes

                                                prefs.edit()
                                                    .putInt(
                                                        "reminderMinutes",
                                                        minutes
                                                    )
                                                    .apply()

                                                if (
                                                    remindersEnabled
                                                ) {
                                                    scheduleReminder(
                                                        this@MainActivity,
                                                        minutes
                                                    )
                                                }

                                                customReminder = ""

                                            } else {

                                                message =
                                                    "Custom reminder must be at least 10 minutes."
                                            }
                                        },
                                        modifier =
                                            Modifier.fillMaxWidth()
                                    ) {
                                        Text("Set Custom Reminder")
                                    }

                                    Text(
                                        "Minimum interval: 10 minutes. Android battery saving may delay notifications.",
                                        style =
                                            MaterialTheme
                                                .typography
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
                                        MaterialTheme
                                            .typography
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
                                                "Age must be between 1 and 120."

                                        } else if (
                                            h !in 50.0..250.0
                                        ) {

                                            message =
                                                "Height must be between 50 and 250 cm."

                                        } else if (
                                            w !in 10.0..300.0
                                        ) {

                                            message =
                                                "Weight must be between 10 and 300 kg."

                                        } else {

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
                                                    a >= 65 -> -100
                                                    else -> 0
                                                }

                                            goal =
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
                                        MaterialTheme
                                            .typography
                                            .titleLarge,
                                    fontWeight =
                                        FontWeight.Bold
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

                                Spacer(
                                    Modifier.height(8.dp)
                                )

                                Text(
                                    "Hydra v1.3",
                                    style =
                                        MaterialTheme.typography
                                            .bodySmall
                                )
                            }
                        }

                        Text(
                            "Hydra • Drink water, stay hydrated.",
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

    private fun reminderText(minutes: Int): String {

        return if (minutes < 60) {
            "Every $minutes minutes"
        } else if (minutes % 60 == 0) {
            "Every ${minutes / 60} hour" +
                if (minutes / 60 == 1) "" else "s"
        } else {
            "Every ${minutes / 60}h ${minutes % 60}m"
        }
    }

    private fun reminderShortText(minutes: Int): String {

        return if (minutes < 60) {
            "${minutes}m"
        } else {
            "${minutes / 60}h"
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
            minutes: Int
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

            alarmManager.cancel(pendingIntent)

            val intervalMillis =
                minutes.toLong() *
                    60L *
                    1000L

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

            alarmManager.cancel(pendingIntent)
        }
    }
}