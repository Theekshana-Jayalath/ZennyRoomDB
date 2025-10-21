package com.example.zenny

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.button.MaterialButtonToggleGroup
import androidx.lifecycle.lifecycleScope
import com.example.zenny.data.DatabaseProvider
import com.example.zenny.data.repository.MoodRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class AddMoodBottomSheet : BottomSheetDialogFragment() {

    private lateinit var emojiButtons: List<Button>
    private lateinit var notesEditText: EditText
    private lateinit var saveButton: View
    private lateinit var toggleGroup: MaterialButtonToggleGroup

    private var selectedEmoji: String = "😐" // Default to neutral
    private var dateIso: String = ""
    private var existingEntry: MoodEntry? = null
    private val repo: MoodRepository by lazy {
        MoodRepository(DatabaseProvider.get(requireContext()))
    }

    // Callback to notify the fragment that data has changed
    var onSaved: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateIso = arguments?.getString(ARG_DATE) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_bottom_sheet_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
    notesEditText = view.findViewById(R.id.editNote)
        saveButton = view.findViewById(R.id.btnSaveMood)
    toggleGroup = view.findViewById(R.id.moodToggleGroup)
        emojiButtons = listOf(
            view.findViewById<Button>(R.id.btnEmoji1),
            view.findViewById<Button>(R.id.btnEmoji2),
            view.findViewById<Button>(R.id.btnEmoji3),
            view.findViewById<Button>(R.id.btnEmoji4),
            view.findViewById<Button>(R.id.btnEmoji5)
        )

        // Default selection (neutral) and single-selection listener
        val defaultId = getEmojiButtonId(selectedEmoji)
        toggleGroup.check(defaultId)
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val btn = view.findViewById<Button>(checkedId)
                selectedEmoji = btn.text.toString()
            }
        }

        saveButton.setOnClickListener {
            saveMoodEntry()
        }

        // Load existing data if available
        loadExistingMood()
    }

    private fun loadExistingMood() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            existingEntry = repo.getForDate(dateIso).firstOrNull()
            // Switch back to the main thread to update UI
            launch(Dispatchers.Main) {
                existingEntry?.let {
                    selectedEmoji = it.emoji
                    notesEditText.setText(it.note)
                    toggleGroup.check(getEmojiButtonId(it.emoji))
                } ?: toggleGroup.check(getEmojiButtonId(selectedEmoji))
            }
        }
    }

    private fun getEmojiButtonId(emoji: String): Int = when (emoji) {
        "😀" -> R.id.btnEmoji1
        "😞" -> R.id.btnEmoji2
        "😐" -> R.id.btnEmoji3
        "😭" -> R.id.btnEmoji4
        "😡" -> R.id.btnEmoji5
        else -> R.id.btnEmoji3 // Default case
    }

    private fun saveMoodEntry() {
        val note = notesEditText.text.toString().trim()
        val entry = MoodEntry(
            id = existingEntry?.id ?: UUID.randomUUID().toString(),
            dateIso = dateIso,
            emoji = selectedEmoji,
            note = note,
            timestamp = System.currentTimeMillis()
        )

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            repo.addOrUpdate(entry)
            // On completion, switch to main thread to trigger callback and dismiss
            launch(Dispatchers.Main) {
                onSaved?.invoke()
                dismiss()
            }
        }
    }

    companion object {
        private const val ARG_DATE = "date"

        fun newInstance(date: String): AddMoodBottomSheet {
            return AddMoodBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_DATE, date)
                }
            }
        }
    }
}
