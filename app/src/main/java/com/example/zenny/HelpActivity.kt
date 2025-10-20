package com.example.zenny

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.zenny.databinding.PopupHelpBinding

class HelpActivity : AppCompatActivity() {

    private lateinit var binding: PopupHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PopupHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClose.setOnClickListener {
            finish() // Closes the activity
        }
    }
}
