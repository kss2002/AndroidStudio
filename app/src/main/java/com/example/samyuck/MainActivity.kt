package com.example.samyuck

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.Serializable
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    //var containerLayout: LinearLayout? = null
    /*
    class ScheduleItem : Serializable {
        var date: String? = null
        var category: String? = null
        var color: Int = 0
    }
    */

    private lateinit var calendarAdapter: CalendarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //containerLayout = findViewById<LinearLayout>(R.id.containerLayout)
        val addButton = findViewById<ImageButton>(R.id.addButton)//
        addButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this@MainActivity,
                CategoryActivity::class.java
            )
            startActivity(intent)
        })


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