package com.example.zenny

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.zenny.preferences.HabitPreferences
import com.example.zenny.preferences.UserPreferences
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class activity_home : AppCompatActivity() {

    private lateinit var habitsContainer: LinearLayout
    private lateinit var addHabitButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressPercentageText: TextView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var homeContent: View
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var layoutTop: View
    private lateinit var userProfileIcon: CircleImageView
    private lateinit var userDisplayedName: TextView

    private lateinit var habitPreferences: HabitPreferences
    private lateinit var userPreferences: UserPreferences
    private var habits = mutableListOf<Habit>()

    private val profileUpdateLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        loadUserProfileData()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. You can now schedule notifications.
                Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show()
            } else {
                // Permission is denied. Show a message to the user.
                Toast.makeText(this, "Notifications permission denied. Reminders will not work.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userPreferences = UserPreferences(this)

        if (!userPreferences.isOnboardingComplete()) {
            val intent = Intent(this, activity_onboard1::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_home)

        // Request notification permission on startup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }


        habitPreferences = HabitPreferences.getInstance(this)

        habitsContainer = findViewById(R.id.habitsContainer)
        addHabitButton = findViewById(R.id.btn_add_box)
        progressBar = findViewById(R.id.progress_bar)
        progressPercentageText = findViewById(R.id.tv_progress_percentage)
        bottomNav = findViewById(R.id.bottom_navigation)
        homeContent = findViewById(R.id.home_content)
        fragmentContainer = findViewById(R.id.fragment_container)
        layoutTop = findViewById(R.id.layoutTop)
        userProfileIcon = findViewById(R.id.user)
        userDisplayedName = findViewById(R.id.tv_displayed_name_home)

        addHabitButton.setOnClickListener { showAddHabitDialog() }

        userProfileIcon.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            profileUpdateLauncher.launch(intent)
        }

        checkDateAndResetProgress()
        loadHabits()
        updateProgress()
        loadUserProfileData()

        bottomNav.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_home -> {
                    showMainContent(true)
                    return@setOnItemSelectedListener true
                }
                R.id.nav_mood -> {
                    selectedFragment = activity_mood.newInstance()
                }
                R.id.nav_water -> {
                    selectedFragment = HydrationFragment.newInstance()
                }
            }

            if (selectedFragment != null) {
                showMainContent(false)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()
            }
            true
        }
    }

    private fun loadUserProfileData() {
        val displayedName = userPreferences.getDisplayedName()
        userDisplayedName.text = if (!displayedName.isNullOrEmpty()) displayedName else "User"

        val imagePath = userPreferences.getProfileImagePath()
        if (!imagePath.isNullOrEmpty()) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                Glide.with(this)
                    .load(imageFile) // Load the File directly
                    .into(userProfileIcon)
            } else {
                userProfileIcon.setImageResource(R.drawable.user) // Fallback if file not found
            }
        } else {
            userProfileIcon.setImageResource(R.drawable.user) // Default image
        }
    }

    private fun checkDateAndResetProgress() {
        val wasReset = habitPreferences.checkAndResetDailyProgress()
        if (wasReset) {
            Toast.makeText(this, "New day! Habits reset for today", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveHabits() {
        habitPreferences.saveHabits(habits)
    }

    private fun loadHabits() {
        habits = habitPreferences.loadHabits()
        habitsContainer.removeAllViews()
        habits.forEach { addHabitView(it) }
    }

    private fun showMainContent(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        homeContent.visibility = visibility
        layoutTop.visibility = visibility

        if (show) {
            fragmentContainer.visibility = View.GONE
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment != null) {
                supportFragmentManager.beginTransaction().remove(currentFragment).commit()
            }
        } else {
            fragmentContainer.visibility = View.VISIBLE
        }
    }

    private fun updateProgress() {
        val progressPercentage = habitPreferences.getProgressPercentage()
        progressBar.progress = progressPercentage
        progressPercentageText.text = "$progressPercentage%"
    }

    private fun showAddHabitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.activity_dialog_add_habit, null)
        val etHabitName = dialogView.findViewById<EditText>(R.id.etHabitName)
        val btnChooseTime = dialogView.findViewById<Button>(R.id.btnChooseTime)
        var selectedTime = ""

        btnChooseTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, h, m ->
                val c = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m) }
                selectedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(c.time)
                btnChooseTime.text = selectedTime
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add Habit")
            .setPositiveButton("Add") { _, _ ->
                val name = etHabitName.text.toString().trim()
                if (name.isNotEmpty()) {
                    val habit = Habit(name, if (selectedTime.isNotEmpty()) selectedTime else "No time set")
                    habits.add(habit)
                    addHabitView(habit)
                    habitPreferences.addHabit(habit)
                    updateProgress()
                    Toast.makeText(this, "Habit added", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter a habit name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addHabitView(habit: Habit) {
        val habitView = layoutInflater.inflate(R.layout.activity_list_item_habit, habitsContainer, false)
        val tvName = habitView.findViewById<TextView>(R.id.habitNameTextView)
        val tvTime = habitView.findViewById<TextView>(R.id.habitTimeTextView)
        val delete = habitView.findViewById<ImageView>(R.id.deleteHabitIcon)
        val edit = habitView.findViewById<ImageView>(R.id.editHabitIcon)
        val checkBox = habitView.findViewById<CheckBox>(R.id.habitCheckBox)

        tvName.text = habit.name
        tvTime.text = habit.time
        checkBox.isChecked = habit.isCompleted

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            habit.isCompleted = isChecked
            habitPreferences.updateHabitCompletion(habit, isChecked)
            updateProgress()
        }
        
        delete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Habit")
                .setMessage("Are you sure you want to delete '${habit.name}'?")
                .setPositiveButton("Delete") { _, _ ->
                    habits.remove(habit)
                    habitsContainer.removeView(habitView)
                    habitPreferences.removeHabit(habit)
                    updateProgress()
                    Toast.makeText(this, "Habit deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        edit.setOnClickListener { showEditHabitDialog(habit, tvName, tvTime) }

        habitsContainer.addView(habitView)
        updateProgress()
    }

    private fun showEditHabitDialog(habit: Habit, tvName: TextView, tvTime: TextView) {
        val dialogView = layoutInflater.inflate(R.layout.activity_dialog_add_habit, null)
        val etName = dialogView.findViewById<EditText>(R.id.etHabitName)
        val btnTime = dialogView.findViewById<Button>(R.id.btnChooseTime)

        etName.setText(habit.name)
        var selectedTime = habit.time
        btnTime.text = if (selectedTime != "No time set") selectedTime else "Choose Time"

        btnTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, h, m ->
                val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m) }
                selectedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time)
                btnTime.text = selectedTime
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Edit Habit")
            .setPositiveButton("Save") { _, _ ->
                habit.name = etName.text.toString().trim()
                habit.time = if (selectedTime.isNotEmpty()) selectedTime else "No time set"
                tvName.text = habit.name
                tvTime.text = habit.time
                updateProgress()
                saveHabits()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
