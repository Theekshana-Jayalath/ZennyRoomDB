package com.example.zenny

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.zenny.data.DatabaseProvider
import com.example.zenny.data.repository.MoodRepository
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters

class MoodBarBottomSheet : BottomSheetDialogFragment() {

    private lateinit var barChart: BarChart
    private val repo by lazy { MoodRepository(DatabaseProvider.get(requireContext())) }

    private var year: Int = 0
    private var month: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        year = requireArguments().getInt(ARG_YEAR)
        month = requireArguments().getInt(ARG_MONTH)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_mood_bar, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        barChart = view.findViewById(R.id.moodBarChart)
        setupChart()
        viewLifecycleOwner.lifecycleScope.launch { loadData() }
    }

    private fun setupChart() {
        barChart.description.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.axisLeft.granularity = 1f
        barChart.axisLeft.axisMinimum = 0f
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setDrawGridLines(false)
        barChart.legend.isEnabled = false
    }

    private suspend fun loadData() {
        val date = LocalDate.of(year, month, 1)
        val start = date.withDayOfMonth(1).toString()
        val end = date.with(TemporalAdjusters.lastDayOfMonth()).toString()
        val moods = repo.getForDateRange(start, end)

        val emojis = listOf("😡", "😭", "😐", "😞", "😀")
        val counts = emojis.map { e -> moods.count { it.emoji == e }.toFloat() }
        val entries = counts.mapIndexed { idx, v -> BarEntry(idx.toFloat(), v) }

        val set = BarDataSet(entries, "").apply {
            color = ContextCompat.getColor(requireContext(), android.R.color.holo_purple)
            valueTextColor = ContextCompat.getColor(requireContext(), android.R.color.black)
            valueTextSize = 12f
        }
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(emojis)
        barChart.data = BarData(set).apply { barWidth = 0.6f }
        barChart.invalidate()
    }

    companion object {
        private const val ARG_YEAR = "year"
        private const val ARG_MONTH = "month"
        fun newInstance(year: Int, month: Int): MoodBarBottomSheet = MoodBarBottomSheet().apply {
            arguments = Bundle().apply {
                putInt(ARG_YEAR, year)
                putInt(ARG_MONTH, month)
            }
        }
    }
}
