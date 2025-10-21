package com.example.zenny.ui.mood

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zenny.R
import com.example.zenny.data.DatabaseProvider
import com.example.zenny.data.repository.MoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MoodActivity : AppCompatActivity() {

    private lateinit var moodRepo: MoodRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        moodRepo = MoodRepository(DatabaseProvider.get(this))

        lifecycleScope.launch(Dispatchers.IO) {
            val allMoods = moodRepo.getAll()
            // TODO: Update RecyclerView adapter on main thread
        }

        // Example: open bottom sheet
        // val bottomSheet = AddMoodBottomSheet.newInstance("2025-10-20")
        // bottomSheet.show(supportFragmentManager, "AddMoodBottomSheet")
    }
}
