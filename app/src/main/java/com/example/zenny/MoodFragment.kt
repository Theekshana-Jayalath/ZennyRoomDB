package com.example.zenny

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.style.LineBackgroundSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.zenny.data.DatabaseProvider
import com.example.zenny.data.repository.MoodRepository
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.launch
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeParseException
import org.threeten.bp.temporal.TemporalAdjusters
import java.util.Locale
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 * A fragment to display a mood calendar.
 */
class MoodFragment : Fragment() {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var moodPieChart: PieChart

    // Use 'by lazy' for cleaner initialization of the repository.
    private val moodRepo: MoodRepository by lazy {
        MoodRepository(DatabaseProvider.get(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mood, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
    calendarView = view.findViewById(R.id.calendarView)
    moodPieChart = view.findViewById(R.id.moodPieChart)

    // Setup UI components
    setupCalendar()
    setupPieChart()

    // Bar View button opens monthly bar chart (resolve ID dynamically to avoid compile-time issues)
    run {
            val ctx = requireContext()
            val barId = resources.getIdentifier("btnBarView", "id", ctx.packageName)
            if (barId != 0) {
                view.findViewById<View>(barId)?.setOnClickListener {
                    val m = calendarView.currentDate ?: CalendarDay.today()
                    MoodBarBottomSheet.newInstance(m.year, m.month).show(childFragmentManager, "MoodBar")
                }
            }
        }

    // Load initial data
    refreshData()
    }

    /**
     *  Refreshes the calendar decorators.
     */
    private fun refreshData() {
        // Use lifecycleScope to launch coroutines safely within the fragment's lifecycle.
        viewLifecycleOwner.lifecycleScope.launch {
            refreshCalendarDecorators()
            val current = calendarView.currentDate ?: CalendarDay.today()
            updateMonthlyPie(current)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh pie in case data changed while paused
        viewLifecycleOwner.lifecycleScope.launch {
            val current = calendarView.currentDate ?: CalendarDay.today()
            updateMonthlyPie(current)
        }
    }

    private fun setupCalendar() {
        calendarView.state().edit()
            .setFirstDayOfWeek(DayOfWeek.MONDAY)
            .commit()

        // Always highlight/select today
    calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE)
        calendarView.selectedDate = CalendarDay.today()

    calendarView.setOnDateChangedListener { widget, date, _ ->
            // Only allow selecting today; revert selection if not today
            val today = CalendarDay.today()
            if (date != today) {
                widget.setDateSelected(date, false)
                widget.selectedDate = today
                return@setOnDateChangedListener
            }

            // Format date correctly for the bottom sheet
            val clickedDate = String.format(Locale.US, "%04d-%02d-%02d", date.year, date.month, date.day)

            val bottomSheet = AddMoodBottomSheet.newInstance(clickedDate)
            // Set a listener to refresh data after a mood is saved.
            bottomSheet.onSaved = { refreshData() }
            bottomSheet.show(childFragmentManager, "AddMoodBottomSheet")
        }

    // Update monthly chart when user navigates months
        calendarView.setOnMonthChangedListener { _, month ->
            viewLifecycleOwner.lifecycleScope.launch { updateMonthlyPie(month) }
        }
    }

    /**
     * Fetches moods from the database and updates the calendar to show dots and emojis.
     */
    private suspend fun refreshCalendarDecorators() {
    calendarView.removeDecorators()
    calendarView.addDecorator(OnlyTodaySelectableDecorator())

        val moods = moodRepo.getAll()
        val moodDecorators = moods.mapNotNull { mood ->
            try {
                // Use ThreeTenBP's LocalDate for parsing, it's safer and more modern.
                val date = LocalDate.parse(mood.dateIso)
                CalendarDay.from(date) to mood.emoji
            } catch (e: DateTimeParseException) {
                // Ignore moods with invalid date formats
                null
            }
        }

        // Add a red dot to every day that has a mood entry.
        val datesWithMoods = moodDecorators.map { it.first }.toSet()
        calendarView.addDecorator(EventDecorator(Color.RED, datesWithMoods))

        // Add a specific emoji decorator for each day.
        moodDecorators.forEach { (date, emoji) ->
            calendarView.addDecorator(EmojiDecorator(date, emoji))
        }
    }

    private fun setupPieChart() {
        moodPieChart.description.isEnabled = false
        moodPieChart.legend.isEnabled = true
        moodPieChart.setUsePercentValues(true)
        moodPieChart.isDrawHoleEnabled = true
        moodPieChart.holeRadius = 45f
        moodPieChart.transparentCircleRadius = 48f
        moodPieChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        moodPieChart.setEntryLabelTextSize(12f)
        moodPieChart.setDrawEntryLabels(true)
        moodPieChart.centerText = ""
        moodPieChart.setCenterTextSize(14f)
        moodPieChart.setNoDataText("No data for this month")
        moodPieChart.setNoDataTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
    }

    private suspend fun updateMonthlyPie(month: CalendarDay) {
        val date = LocalDate.of(month.year, month.month, 1)
        val start = date.withDayOfMonth(1)
        val end = date.with(TemporalAdjusters.lastDayOfMonth())

        val moods = moodRepo.getForDateRange(start.toString(), end.toString())
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        // Define colors per emoji for consistency
        val emojiColors = mapOf(
            "😡" to ContextCompat.getColor(requireContext(), android.R.color.holo_red_light),
            "😭" to ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light),
            "😐" to ContextCompat.getColor(requireContext(), android.R.color.darker_gray),
            "😞" to ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light),
            "😀" to ContextCompat.getColor(requireContext(), android.R.color.holo_green_light)
        )

        val emojis = listOf("😡", "😭", "😐", "😞", "😀")
        emojis.forEach { emoji ->
            val count = moods.count { it.emoji == emoji }
            if (count > 0) {
                entries.add(PieEntry(count.toFloat(), emoji))
                colors.add(emojiColors[emoji] ?: ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            }
        }

        val dataSet = PieDataSet(entries, "Monthly Mood Share").apply {
            sliceSpace = 2f
            setDrawValues(true)
            valueTextColor = ContextCompat.getColor(requireContext(), android.R.color.black)
            valueTextSize = 12f
            this.colors = colors
        }

        val pieData = PieData(dataSet)
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value < 1f) "" else "${value.toInt()}%"
            }
        }

        if (entries.isEmpty()) {
            val placeholder = PieDataSet(
                emojis.map { PieEntry(1f, it) },
                "No data yet"
            ).apply {
                this.colors = emojis.map { ContextCompat.getColor(requireContext(), android.R.color.darker_gray) }
                setDrawValues(false)
            }
            moodPieChart.data = PieData(placeholder)
            moodPieChart.centerText = "0 entries"
        } else {
            moodPieChart.data = pieData
            moodPieChart.centerText = "${moods.size} entries"
        }
        moodPieChart.invalidate()
    }

    // --- Calendar Decorator Inner Classes ---

    /** Disables interaction with all dates except today. */
    private class OnlyTodaySelectableDecorator : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = day != CalendarDay.today()
        override fun decorate(view: DayViewFacade) {
            view.setDaysDisabled(true)
        }
    }

    /** Adds a colored dot below the date number. */
    private class EventDecorator(private val color: Int, private val dates: Set<CalendarDay>) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)
        override fun decorate(view: DayViewFacade) {
            view.addSpan(DotSpan(5f, color))
        }
    }

    /** Adds the emoji text below the date number using a custom span. */
    private class EmojiDecorator(private val date: CalendarDay, private val emoji: String) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = day == date
        override fun decorate(view: DayViewFacade) {
            // Use a custom span to draw the emoji. The size can be adjusted.
            view.addSpan(CustomTextSpan(emoji, 40f))
        }
    }

    companion object {
        /** Use this factory method to create a new instance of this fragment. */
        @JvmStatic
        fun newInstance() = MoodFragment()
    }
}

/**
 * A custom LineBackgroundSpan to draw text (like an emoji) on the calendar day.
 * This span draws the provided text below the day number.
 */
private class CustomTextSpan(
    private val text: String,
    private val textSize: Float
) : LineBackgroundSpan {

    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {
        val oldTextSize = paint.textSize
        val oldAlign = paint.textAlign
        paint.textSize = textSize
        paint.textAlign = Paint.Align.CENTER

        // Calculate position to draw the emoji.
        // Horizontally centered, and vertically positioned below the day number's area.
        val x = (left + right) / 2f
        val y = bottom + textSize * 0.8f // Adjust the 0.8f factor for desired vertical spacing

        canvas.drawText(this.text, x, y, paint)

        // Restore original paint settings
        paint.textSize = oldTextSize
        paint.textAlign = oldAlign
    }
}
