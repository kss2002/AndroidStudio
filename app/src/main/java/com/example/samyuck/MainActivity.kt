package com.example.samyuck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var calendarAdapter: CalendarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupCalendar()
    }

    private fun setupCalendar() {
        val recyclerView = findViewById<RecyclerView>(R.id.calendarGrid)
        calendarAdapter = CalendarAdapter()

        recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 7)
            adapter = calendarAdapter
        }

        // Set current month
        val calendar = Calendar.getInstance()
        calendarAdapter.setCalendarDays(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH)
        )
    }
}