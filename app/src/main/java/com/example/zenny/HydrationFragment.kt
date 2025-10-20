package com.example.zenny

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class HydrationFragment : Fragment() {

    private lateinit var halfProgress: HalfCircleProgress
    private lateinit var progressTextView: TextView
    private lateinit var plusButton: ImageButton
    private lateinit var centerButton: ImageButton
    private lateinit var countdownTextView: TextView
    private lateinit var reminderSpinner: Spinner
    private lateinit var countDownProgress: CircularProgressIndicator
    private lateinit var glassSizeTextView: TextView
    private lateinit var refreshButton: ImageButton

    private var dailyGoal: Int = 0
    private var currentIntake: Int = 0
    private var lastAddedAmount: Int = 0
    private var wakingHours: Int = 8
    private var currentCountdownTotalDuration: Long = 0L
    private var pendingReminderInterval: Long = 0L

    private lateinit var prefs: SharedPreferences


    private val reminderOptions = linkedMapOf(
        "Off" to 0L,
        "10 Seconds (Test)" to 10 * 1000L,
        "15 minutes" to 15 * 60 * 1000L,
        "30 minutes" to 30 * 60 * 1000L,
        "45 minutes" to 45 * 60 * 1000L,
        "1 hour" to 60 * 60 * 1000L,
        "1 hour 30 minutes" to 90 * 60 * 1000L,
        "2 hours" to 120 * 60 * 1000L,
        "2 hour 30 minutes" to 150 * 60 * 1000L,
        "3 hours" to 180 * 60 * 1000L
    )

    private val countdownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == WaterReminderService.COUNTDOWN_TICK) {
                val timeRemaining = intent.getLongExtra(WaterReminderService.EXTRA_TIME_REMAINING, 0)
                if (currentCountdownTotalDuration > 0) {
                    updateCountdownUI(timeRemaining, currentCountdownTotalDuration)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hydration, container, false)
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        initializeViews(view)
        createNotificationChannel()
        setupClickListeners()

        loadData()

        setupSpinnerListener()

        return view
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(countdownReceiver, IntentFilter(WaterReminderService.COUNTDOWN_TICK))
        resumeCountdownState()
    }

    override fun onPause() {
        super.onPause()
        saveData()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(countdownReceiver)
    }

    private fun initializeViews(view: View) {
        halfProgress = view.findViewById(R.id.half_progress)
        progressTextView = view.findViewById(R.id.progress_text)
        plusButton = view.findViewById(R.id.plusButton)
        centerButton = view.findViewById(R.id.centerButton)
        countdownTextView = view.findViewById(R.id.countdown_text)
        reminderSpinner = view.findViewById(R.id.reminder_spinner)
        countDownProgress = view.findViewById(R.id.countDownloadProgress)
        glassSizeTextView = view.findViewById(R.id.glass_size_text) // Initialize the new TextView
        refreshButton = view.findViewById(R.id.refresh_button)
    }

    private fun setupClickListeners() {
        plusButton.setOnClickListener {
            showSetGoalDialog()
        }

        centerButton.setOnClickListener {
            if (dailyGoal <= 0 || lastAddedAmount <= 0) {
                Toast.makeText(requireContext(), "Please set your water goal first.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (currentIntake >= dailyGoal) {
                Toast.makeText(requireContext(), "Congratulations! You've reached your daily goal!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentIntake += lastAddedAmount
            updateIntakeUI()


            val reminderInterval = prefs.getLong(KEY_REMINDER_INTERVAL, 0)
            if (reminderInterval > 0) {
                startReminders(reminderInterval)
            }
        }

        refreshButton.setOnClickListener {
            currentIntake = 0
            dailyGoal = 0
            lastAddedAmount = 0
            wakingHours = 8
            updateIntakeUI()
            updateGlassSizeText()
            reminderSpinner.setSelection(0)
            stopReminders()
            Toast.makeText(requireContext(), "Hydration data has been reset.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinnerListener() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, reminderOptions.keys.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reminderSpinner.adapter = adapter

        val reminderPosition = prefs.getInt(KEY_REMINDER_POS, 0)
        reminderSpinner.setSelection(reminderPosition, false)

        reminderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedInterval = reminderOptions.values.toList()[position]
                if (selectedInterval > 0) {

                    if (areNotificationsEnabled()) {
                        checkPermissionsAndStartReminders(selectedInterval)
                    } else {
                        Toast.makeText(requireContext(), "Notifications are disabled in system settings. Please enable them for reminders to work.", Toast.LENGTH_LONG).show()
                        reminderSpinner.setSelection(0) // Reset to "Off"
                    }
                } else {
                    stopReminders()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
    }

    private fun checkPermissionsAndStartReminders(interval: Long) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                pendingReminderInterval = interval
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(requireContext(), "Please enable exact alarms in system settings for reminders to work properly.", Toast.LENGTH_LONG).show()

                return
            }
        }
        

        startReminders(interval)
    }

    private fun startReminders(interval: Long) {
        val endTime = System.currentTimeMillis() + interval
        currentCountdownTotalDuration = interval
        prefs.edit().apply {
            putBoolean(KEY_REMINDERS_ENABLED, true)
            putLong(KEY_REMINDER_INTERVAL, interval)
            putLong(KEY_REMINDER_END_TIME, endTime)
            putInt(KEY_REMINDER_POS, reminderSpinner.selectedItemPosition)
            apply()
        }

        calculateAndSetDailyGoal(interval)
        startCountdownService(interval, interval)

        val alarmIntent = Intent(requireContext(), ReminderBroadcastReceiver::class.java).apply {
            putExtra("EXTRA_INTERVAL", interval)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), REQUEST_CODE_ALARM, alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(requireContext(), "Cannot schedule exact alarms.", Toast.LENGTH_SHORT).show()
            return
        }
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTime, pendingIntent)

        Toast.makeText(requireContext(), "Goal updated to ${dailyGoal}ml. Reminders set.", Toast.LENGTH_SHORT).show()
    }

    private fun startCountdownService(totalDuration: Long, remainingTime: Long) {
        val serviceIntent = Intent(requireContext(), WaterReminderService::class.java).apply {
            action = WaterReminderService.ACTION_START_OR_UPDATE_TIMER
            putExtra(WaterReminderService.EXTRA_INITIAL_TIME, totalDuration)
            putExtra(WaterReminderService.EXTRA_INTERVAL, remainingTime)
        }
        requireContext().startService(serviceIntent)
    }

    private fun stopReminders() {
        currentCountdownTotalDuration = 0L
        prefs.edit().apply{
            putBoolean(KEY_REMINDERS_ENABLED, false)
            putLong(KEY_REMINDER_END_TIME, 0)
            putInt(KEY_REMINDER_POS, 0)
            apply()
        }

        calculateAndSetDailyGoal(0L)
        Toast.makeText(requireContext(), "Reminders off. Goal reset to ${dailyGoal}ml.", Toast.LENGTH_SHORT).show()

        val serviceIntent = Intent(requireContext(), WaterReminderService::class.java).apply {
            action = WaterReminderService.ACTION_STOP_TIMER
        }
        requireContext().startService(serviceIntent)
        countdownTextView.text = "00:00:00"
        countDownProgress.progress = 0

        val alarmIntent = Intent(requireContext(), ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            REQUEST_CODE_ALARM,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun showSetGoalDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_water, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val waterAmountEditText = dialogView.findViewById<EditText>(R.id.waterAmountEditText)
        val hoursSpinner = dialogView.findViewById<Spinner>(R.id.hoursSpinner)
        val addButton = dialogView.findViewById<Button>(R.id.addButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)


        val hoursOptions = (8..18).map { "$it hours" }
        val hoursAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, hoursOptions)
        hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hoursSpinner.adapter = hoursAdapter

        waterAmountEditText.setText(if (lastAddedAmount > 0) lastAddedAmount.toString() else "")

        addButton.setOnClickListener {
            val glassSize = waterAmountEditText.text.toString().toIntOrNull()
            val selectedHours = hoursSpinner.selectedItem.toString().split(" ")[0].toInt()

            if (glassSize != null && glassSize > 0) {
                lastAddedAmount = glassSize
                this.wakingHours = selectedHours
                currentIntake = 0

                updateIntakeUI()
                updateGlassSizeText()


                val selectedReminderPosition = reminderSpinner.selectedItemPosition
                val selectedInterval = reminderOptions.values.toList()[selectedReminderPosition]
                calculateAndSetDailyGoal(selectedInterval)
                if (selectedInterval > 0) {
                    Toast.makeText(requireContext(), "Goal updated to ${dailyGoal}ml.", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter a valid glass size", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun calculateAndSetDailyGoal(reminderInterval: Long) {
        if (lastAddedAmount > 0 && wakingHours > 0) {
            if (reminderInterval > 0) {
                // For goal calculation, use a realistic minimum interval (e.g., 15 mins)
                // to avoid absurdly large goals when using short test timers like "10 seconds".
                // The actual reminders will still fire at the user-selected interval.
                val minRealisticInterval = 15 * 60 * 1000L // 15 minutes
                val calculationInterval = if (reminderInterval < minRealisticInterval) minRealisticInterval else reminderInterval

                val wakingHoursInMillis = wakingHours * 60 * 60 * 1000L
                val numberOfDrinks = wakingHoursInMillis / calculationInterval
                dailyGoal = (numberOfDrinks * lastAddedAmount).toInt()
            } else {

                dailyGoal = lastAddedAmount * wakingHours
            }
        } else {

            dailyGoal = 0
        }
        updateIntakeUI()
    }

    private fun updateIntakeUI() {
        if (dailyGoal > 0) {
            currentIntake = currentIntake.coerceAtMost(dailyGoal)
        }
        progressTextView.text = "$currentIntake/${dailyGoal}ml"
        val progressPercentage = if (dailyGoal > 0) (currentIntake.toFloat() / dailyGoal.toFloat() * 100).toInt() else 0
        halfProgress.setProgress(progressPercentage)
    }

    private fun updateGlassSizeText() {
        glassSizeTextView.text = if (lastAddedAmount > 0) "${lastAddedAmount}ml" else ""
    }

    private fun updateCountdownUI(timeRemaining: Long, initialTime: Long) {
        val seconds = timeRemaining / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        countdownTextView.text = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)

        val progress = if (initialTime > 0) {
            val elapsed = initialTime - timeRemaining
            (elapsed.toFloat() / initialTime.toFloat() * 100).toInt()
        } else {
            0
        }
        countDownProgress.progress = progress.coerceIn(0, 100)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hydration Reminders"
            val descriptionText = "Notifications to remind you to drink water"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("HYDRATION_CHANNEL_ID", name, importance).apply { 
                description = descriptionText
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Toast.makeText(requireContext(), "Notification permission granted! Setting up reminders...", Toast.LENGTH_SHORT).show()

            if (pendingReminderInterval > 0) {
                startReminders(pendingReminderInterval)
                pendingReminderInterval = 0L // Reset after use
            } else {

                val selectedInterval = prefs.getLong(KEY_REMINDER_INTERVAL, 0)
                if (selectedInterval > 0) {
                    startReminders(selectedInterval)
                }
            }
        } else {
            Toast.makeText(requireContext(), "Notification permission denied. Reminders will not work. Please enable notifications in app settings.", Toast.LENGTH_LONG).show()

            reminderSpinner.setSelection(0)
            stopReminders()
        }
    }

    private fun saveData() {
        prefs.edit().apply {
            putInt(KEY_INTAKE, currentIntake)
            putInt(KEY_GOAL, dailyGoal)
            putInt(KEY_GLASS_SIZE, lastAddedAmount)
            putInt(KEY_WAKING_HOURS, wakingHours)
            putString(KEY_DATE, getCurrentDateString())
            apply()
        }
    }

    private fun loadData() {
        val lastSavedDate = prefs.getString(KEY_DATE, "")
        val todayDate = getCurrentDateString()

        if (lastSavedDate == todayDate) {
            currentIntake = prefs.getInt(KEY_INTAKE, 0)
        } else {
            currentIntake = 0
            // New day: Stop yesterday's reminders and reset for today.
            prefs.edit().apply {
                putBoolean(KEY_REMINDERS_ENABLED, false)
                putLong(KEY_REMINDER_END_TIME, 0)
                putInt(KEY_REMINDER_POS, 0) // This will make the spinner select "Off"
                apply()
            }

            val serviceIntent = Intent(requireContext(), WaterReminderService::class.java).apply {
                action = WaterReminderService.ACTION_STOP_TIMER
            }
            requireContext().startService(serviceIntent)


            val alarmIntent = Intent(requireContext(), ReminderBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                REQUEST_CODE_ALARM,
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )
            if (pendingIntent != null) {
                val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
        lastAddedAmount = prefs.getInt(KEY_GLASS_SIZE, 0)
        wakingHours = prefs.getInt(KEY_WAKING_HOURS, 8)



        val reminderPosition = prefs.getInt(KEY_REMINDER_POS, 0)
        val selectedInterval = reminderOptions.values.toList().getOrElse(reminderPosition) { 0L }
        calculateAndSetDailyGoal(selectedInterval)

        updateIntakeUI()
        updateGlassSizeText()
    }

    private fun resumeCountdownState() {
        if (prefs.getBoolean(KEY_REMINDERS_ENABLED, false)) {
            val originalInterval = prefs.getLong(KEY_REMINDER_INTERVAL, 0)
            val endTime = prefs.getLong(KEY_REMINDER_END_TIME, 0)

            if (originalInterval > 0 && endTime > 0) {
                currentCountdownTotalDuration = originalInterval
                val remainingTime = endTime - System.currentTimeMillis()
                if (remainingTime > 0) {

                    startCountdownService(originalInterval, remainingTime)
                } else {

                    startReminders(originalInterval)
                }
            }
        }
    }

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    companion object {
        private const val PREFS_NAME = "HydrationPrefs"
        internal const val REQUEST_CODE_ALARM = 101

        private const val KEY_INTAKE = "intake"
        private const val KEY_GOAL = "goal"
        private const val KEY_GLASS_SIZE = "glass_size"
        private const val KEY_WAKING_HOURS = "waking_hours"
        private const val KEY_DATE = "date"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        private const val KEY_REMINDER_INTERVAL = "reminder_interval"
        private const val KEY_REMINDER_POS = "reminder_pos"
        private const val KEY_REMINDER_END_TIME = "reminder_end_time"

        @JvmStatic
        fun newInstance() = HydrationFragment()
    }
}
