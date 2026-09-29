package dev.ragnarok.fenrir.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import dev.ragnarok.fenrir.util.Logger
import dev.ragnarok.fenrir.util.UnixTime
import java.util.Calendar
import java.util.Date

class DateTimePicker internal constructor(builder: Builder) {
    private val timeMillis: Long = builder.timeMillis
    private val context: Context = builder.context
    private val callback: Callback? = builder.callback
    private val onlyDate: Boolean = builder.onlyDate
    internal fun show() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]
        val hours = calendar[Calendar.HOUR_OF_DAY]
        val minutes = calendar[Calendar.MINUTE]
        Logger.d(TAG, "onTimerClick, init time: " + Date(timeMillis))
        DatePickerDialog(
            context,
            { _, newYear, newMonth, newDay ->
                if (onlyDate) {
                    callback?.onDateTimeSelected(
                        UnixTime.of(
                            newYear,
                            newMonth,
                            newDay,
                            0,
                            0
                        )
                    )
                } else {
                    showTime(newYear, newMonth, newDay, hours, minutes)
                }
            },
            year,
            month,
            day
        ).show()
    }

    private fun showTime(year: Int, month: Int, day: Int, hour: Int, minutes: Int) {
        TimePickerDialog(
            context,
            { _, newHourOfDay, newMinutes ->
                callback?.onDateTimeSelected(
                    UnixTime.of(
                        year,
                        month,
                        day,
                        newHourOfDay,
                        newMinutes
                    )
                )
            },
            hour,
            minutes,
            true
        ).show()
    }

    interface Callback {
        fun onDateTimeSelected(unixTime: Long)
    }

    class Builder(val context: Context) {
        var callback: Callback? = null
            private set
        var timeMillis: Long
            private set
        var onlyDate: Boolean
            private set

        fun setTime(unixTime: Long): Builder {
            timeMillis = unixTime * 1000
            return this
        }

        fun setOnlyDate(onlyDate: Boolean): Builder {
            this.onlyDate = onlyDate
            return this
        }

        fun setCallback(callback: Callback?): Builder {
            this.callback = callback
            return this
        }

        fun show() {
            DateTimePicker(this).show()
        }

        init {
            timeMillis = System.currentTimeMillis()
            onlyDate = false
        }
    }

    companion object {
        private val TAG = DateTimePicker::class.simpleName.orEmpty()
    }

}