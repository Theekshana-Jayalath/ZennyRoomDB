package com.example.zenny

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.zenny.databinding.PopupHelpBinding

class HelpActivity : AppCompatActivity() {

    private lateinit var binding: PopupHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using View Binding, which is the correct and modern way for this project.
        binding = PopupHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the click listener on the button using the safe binding object. This resolves the crash.
        binding.btnClose.setOnClickListener {
            finish() // Closes the activity
        }
    }
}
