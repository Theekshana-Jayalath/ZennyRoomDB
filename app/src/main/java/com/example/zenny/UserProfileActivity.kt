package com.example.zenny

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.example.zenny.databinding.ActivityUserProfileBinding
import com.example.zenny.preferences.UserPreferences
import com.example.zenny.utils.ImageUtils
import java.io.File

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var userPreferences: UserPreferences
    private var newProfileImageFile: File? = null

    // Modern way to handle activity results
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Copy the selected image to internal storage to get a permanent path
                newProfileImageFile = ImageUtils.copyImageToInternalStorage(this, uri)
                // Load the newly copied image file into the ImageView
                Glide.with(this)
                    .load(newProfileImageFile)
                    .into(binding.ivUserProfile)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)

        loadUserProfile()
        setupDarkModeSwitch()

        binding.tvChangePhoto.setOnClickListener {
            pickImageFromGallery()
        }

        binding.btnSave.setOnClickListener {
            saveUserProfile()
        }

        binding.cardHelp.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupDarkModeSwitch() {
        // Set the switch to the current theme state
        binding.switchDarkMode.isChecked = userPreferences.isDarkMode()

        // Listen for changes on the switch
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Save the new preference
            userPreferences.saveDarkMode(isChecked)

            // Apply the new theme
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            // The activity will be recreated automatically to apply the new theme
        }
    }

    private fun loadUserProfile() {
        binding.etName.setText(userPreferences.getName())
        binding.etDisplayedName.setText(userPreferences.getDisplayedName())
        binding.etEmail.setText(userPreferences.getEmail())

        val imagePath = userPreferences.getProfileImagePath()
        if (imagePath != null) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                Glide.with(this)
                    .load(imageFile)
                    .into(binding.ivUserProfile)
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun saveUserProfile() {
        userPreferences.saveName(binding.etName.text.toString())
        userPreferences.saveDisplayedName(binding.etDisplayedName.text.toString())
        userPreferences.saveEmail(binding.etEmail.text.toString())

        // If a new image was selected and copied, save its permanent path
        newProfileImageFile?.let {
            userPreferences.saveProfileImagePath(it.absolutePath)
        }

        Toast.makeText(this, "Profile Saved", Toast.LENGTH_SHORT).show()

        // Set the result to OK to notify the calling activity (activity_home) that data has changed
        setResult(Activity.RESULT_OK)
        finish() // Close the activity
    }
}
