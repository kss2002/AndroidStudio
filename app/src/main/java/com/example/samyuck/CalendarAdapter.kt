package com.example.samyuck

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class CalendarAdapter : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {
    private var days: List<CalendarDay> = emptyList()
    private var selectedPosition = -1

    // 날짜 클릭 리스너 인터페이스 정의
    interface OnDateClickListener {
        fun onDateClick(date: Date)
    }

    private var onDateClickListener: OnDateClickListener? = null

    fun setOnDateClickListener(listener: OnDateClickListener) {
        this.onDateClickListener = listener
    }

    data class CalendarDay(
        val date: Date,
        val dayOfMonth: Int,
        val isCurrentMonth: Boolean
    )

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayText: TextView = view.findViewById(R.id.dayText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = days[position]
        holder.dayText.apply {
            text = day.dayOfMonth.toString()
            isSelected = position == selectedPosition

            val calendar = Calendar.getInstance().apply { time = day.date }
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            setTextColor(when {
                !day.isCurrentMonth -> Color.LTGRAY
                isSelected -> Color.WHITE
                dayOfWeek == Calendar.SUNDAY -> Color.RED
                dayOfWeek == Calendar.SATURDAY -> Color.BLUE
                else -> Color.BLACK
            })
        }

        holder.itemView.setOnClickListener {
            val oldPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)

            // ✅ 콜백으로 날짜 전달
            onDateClickListener?.onDateClick(day.date)
        }
    }

    override fun getItemCount(): Int = days.size

    fun setCalendarDays(year: Int, month: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)

        val days = mutableListOf<CalendarDay>()

        // 이전 달
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        calendar.add(Calendar.DAY_OF_MONTH, -(firstDayOfWeek - 1))
        for (i in 0 until firstDayOfWeek - 1) {
            days.add(CalendarDay(calendar.time, calendar.get(Calendar.DAY_OF_MONTH), false))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // 현재 달
        calendar.set(year, month, 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..daysInMonth) {
            days.add(CalendarDay(calendar.time, i, true))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // 다음 달
        val remainingDays = 42 - days.size
        for (i in 0 until remainingDays) {
            days.add(CalendarDay(calendar.time, calendar.get(Calendar.DAY_OF_MONTH), false))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        this.days = days
        notifyDataSetChanged()
    }

    fun getDays(): List<CalendarDay> = days
}
