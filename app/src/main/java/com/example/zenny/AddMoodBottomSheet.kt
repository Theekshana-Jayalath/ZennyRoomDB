package com.example.zenny

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.zenny.data.DatabaseProvider
import com.example.zenny.data.repository.MoodRepository
import java.util.UUID

class AddMoodBottomSheet : BottomSheetDialogFragment() {

    var onSaved: ((MoodEntry) -> Unit)? = null

    private var selectedEmoji: String = "😀"
    private lateinit var dateIso: String
    private var repo: MoodRepository? = null
    private var existingEntry: MoodEntry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateIso = requireArguments().getString(ARG_DATE) ?: ""
        if (repo == null) {
            // Fallback in case not provided via newInstance
            repo = MoodRepository(DatabaseProvider.get(requireContext()))
        }
        kotlinx.coroutines.runBlocking {
            existingEntry = repo?.getForDate(dateIso)?.firstOrNull()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_bottom_sheet_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btn1 = view.findViewById<Button>(R.id.btnEmoji1)
        val btn2 = view.findViewById<Button>(R.id.btnEmoji2)
        val btn3 = view.findViewById<Button>(R.id.btnEmoji3)
        val btn4 = view.findViewById<Button>(R.id.btnEmoji4)
        val btn5 = view.findViewById<Button>(R.id.btnEmoji5)
        val note = view.findViewById<EditText>(R.id.editNote)
        val save = view.findViewById<Button>(R.id.btnSaveMood)

        existingEntry?.let {
            selectedEmoji = it.emoji
            note.setText(it.note)
        }

        fun select(emoji: String) { selectedEmoji = emoji }

        btn1.setOnClickListener { select(btn1.text.toString()) }
        btn2.setOnClickListener { select(btn2.text.toString()) }
        btn3.setOnClickListener { select(btn3.text.toString()) }
        btn4.setOnClickListener { select(btn4.text.toString()) }
        btn5.setOnClickListener { select(btn5.text.toString()) }

        save.setOnClickListener {
            val entry = MoodEntry(
                id = existingEntry?.id ?: UUID.randomUUID().toString(),
                dateIso = dateIso,
                emoji = selectedEmoji,
                note = note.text?.toString()?.trim().orEmpty(),
                timestamp = System.currentTimeMillis()
            )
            onSaved?.invoke(entry)
            // Also persist defensively in case caller forgets
            try {
                kotlinx.coroutines.runBlocking { repo?.addOrUpdate(entry) }
            } catch (_: Exception) {}
            dismiss()
        }
    }

    companion object {
        private const val ARG_DATE = "arg_date"

        fun newInstance(dateIso: String, moodRepo: MoodRepository? = null): AddMoodBottomSheet {
            val f = AddMoodBottomSheet()
            f.arguments = Bundle().apply { putString(ARG_DATE, dateIso) }
            f.repo = moodRepo
            return f
        }
    }
}
