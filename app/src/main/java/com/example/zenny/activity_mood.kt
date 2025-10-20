package com.example.zenny

import android.app.Dialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.style.LineBackgroundSpan
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.TemporalAdjusters
import java.text.SimpleDateFormat
import java.util.*

import com.example.zenny.data.DatabaseProvider
import com.example.zenny.data.repository.MoodRepository

class activity_mood : Fragment() {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var barChart: BarChart
    private lateinit var moodRepo: MoodRepository

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View? {
        val v = inflater.inflate(R.layout.activity_mood, container, false)

        moodRepo = MoodRepository(DatabaseProvider.get(requireContext()))
        initializeViews(v)
        setupListeners()
        setupBarChart()
        refreshAll()

        return v
    }

    private fun initializeViews(v: android.view.View) {
        calendarView = v.findViewById(R.id.calendarView)
        barChart = v.findViewById(R.id.barChart)
        calendarView.selectionColor = Color.TRANSPARENT
    }

    private fun setupListeners() {
        calendarView.setOnDateChangedListener { _, date, _ ->
            calendarView.clearSelection()
            if (date.isAfter(CalendarDay.today())) {
                Toast.makeText(requireContext(), "Cannot add moods for future dates", Toast.LENGTH_SHORT).show()
                return@setOnDateChangedListener
            }

            val clickedDate = String.format("%04d-%02d-%02d", date.year, date.month, date.day)
            val bs = AddMoodBottomSheet.newInstance(clickedDate, moodRepo) // ✅ fixed here
            bs.onSaved = { entry ->
                kotlinx.coroutines.runBlocking { moodRepo.addOrUpdate(entry) }
                Toast.makeText(requireContext(), "Mood saved!", Toast.LENGTH_SHORT).show()
                refreshAll()
            }
            bs.show(childFragmentManager, "addMood")
        }

        calendarView.setOnMonthChangedListener { _, date ->
            updateMonthlyAnalysis(date)
        }
    }

    private fun refreshAll() {
        refreshCalendarDecorators()
        updateMonthlyAnalysis(calendarView.currentDate)
    }

    private fun updateMonthlyAnalysis(day: CalendarDay) {
        val date = LocalDate.of(day.year, day.month, day.day)
        val startOfMonth = date.withDayOfMonth(1)
        val endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth())
        val moods = kotlinx.coroutines.runBlocking {
            moodRepo.getForDateRange(
                startOfMonth.toString(),
                endOfMonth.toString()
            )
        }
        barChart.axisRight.axisMaximum = 31f
        barChart.axisLeft.axisMaximum = 31f
        updateChartData(moods)
    }

    private fun updateChartData(moods: List<MoodEntry>) {
        val emojiCounts = EMOJI_LIST.associateWith { emoji ->
            moods.count { it.emoji == emoji }
        }

        val entries = EMOJI_LIST.mapIndexed { index, emoji ->
            BarEntry(index.toFloat(), (emojiCounts[emoji] ?: 0).toFloat())
        }

        val dataSet = BarDataSet(entries, "Mood Count")
        dataSet.colors = listOf(
            Color.parseColor("#FFD700"), // 😡
            Color.parseColor("#F08080"), // 😭
            Color.parseColor("#ADD8E6"), // 😐
            Color.parseColor("#90EE90"), // 😞
            Color.parseColor("#FFA07A")  // 😀
        )
        dataSet.setDrawValues(true)
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value > 0) value.toInt().toString() else ""
            }
        }

    val barData = BarData(dataSet)
    barData.barWidth = 0.8f

    barChart.data = barData
    // Fit to bar width so first/last categories are fully visible
    barChart.setFitBars(true)
    barChart.invalidate()
    }

    private fun setupBarChart() {
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawValueAboveBar(true)
        barChart.setTouchEnabled(false)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
    xAxis.granularity = 1f
    xAxis.isGranularityEnabled = true
        xAxis.valueFormatter = IndexAxisValueFormatter(EMOJI_LIST)
        xAxis.textSize = 12f
        xAxis.setLabelCount(EMOJI_LIST.size, true)
    // Single dataset: do not center labels; show one label per index and avoid clipping ends
    xAxis.setCenterAxisLabels(false)
    xAxis.setAvoidFirstLastClipping(true)
        xAxis.axisMinimum = -0.5f
        xAxis.axisMaximum = EMOJI_LIST.size - 0.5f

        barChart.axisLeft.isEnabled = false
        val yAxisRight = barChart.axisRight
        yAxisRight.axisMinimum = 0f
        yAxisRight.granularity = 1f
        yAxisRight.setDrawGridLines(false)

    // setFitBars is called after setting data in updateChartData()
    }

    private fun refreshCalendarDecorators() {
        calendarView.removeDecorators()
        calendarView.addDecorator(DisablePastDatesDecorator())
        val moods = kotlinx.coroutines.runBlocking { moodRepo.getAll() }
        moods.forEach { mood ->
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(mood.dateIso)
            date?.let {
                val threeTenLocalDate =
                    org.threeten.bp.Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).toLocalDate()
                val calendarDay = CalendarDay.from(threeTenLocalDate)
                calendarView.addDecorator(EmojiDecorator(calendarDay, mood.emoji))
            }
        }
        calendarView.addDecorator(TodayDecorator())
    }

    private inner class DisablePastDatesDecorator : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = day.isBefore(CalendarDay.today())
        override fun decorate(view: DayViewFacade) {
            view.setDaysDisabled(true)
        }
    }

    private inner class EmojiDecorator(private val date: CalendarDay, private val emoji: String) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = day == date
        override fun decorate(view: DayViewFacade) {
            view.addSpan(CustomTextSpan(emoji))
        }
    }

    private inner class TodayDecorator : DayViewDecorator {
        private val today = CalendarDay.today()
        private val highlightDrawable: Drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#E0E0E0"))
        }

        override fun shouldDecorate(day: CalendarDay): Boolean = day == today
        override fun decorate(view: DayViewFacade) {
            view.setBackgroundDrawable(highlightDrawable)
        }
    }

    private inner class CustomTextSpan(private val text: String) : LineBackgroundSpan {
        override fun drawBackground(
            canvas: Canvas, paint: Paint,
            left: Int, right: Int, top: Int, baseline: Int, bottom: Int,
            text: CharSequence, start: Int, end: Int, lnum: Int
        ) {
            val oldTextSize = paint.textSize
            val oldAlign = paint.textAlign
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = oldTextSize * 1.2f
            val y = (baseline + oldTextSize * 1.1).toFloat()
            val x = (left + right) / 2f
            canvas.drawText(this.text, x, y, paint)
            paint.textSize = oldTextSize
            paint.textAlign = oldAlign
        }
    }

    companion object {
        val EMOJI_LIST = listOf("😡", "😭", "😐", "😞", "😀")
        fun newInstance(): activity_mood = activity_mood()
    }
}
