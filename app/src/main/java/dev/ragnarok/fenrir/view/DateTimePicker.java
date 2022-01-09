package dev.ragnarok.fenrir.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;

import java.util.Calendar;
import java.util.Date;

import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.Unixtime;

public class DateTimePicker {

    private static final String TAG = DateTimePicker.class.getSimpleName();

    private final long time;
    private final Context context;
    private final Callback callback;

    private DateTimePicker(Builder builder) {
        time = builder.time;
        context = builder.context;
        callback = builder.callback;
    }

    private void show() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        Logger.d(TAG, "onTimerClick, init time: " + new Date(time));

        new DatePickerDialog(context, (view, newYear, newMonth, newDay) ->
                showTime(newYear, newMonth, newDay, hours, minutes), year, month, day).show();
    }

    private void showTime(int year, int month, int day, int hour, int minutes) {
        new TimePickerDialog(context, (view, newHourOfDay, newMinutes) ->
                callback.onDateTimeSelected(Unixtime.of(year, month, day, newHourOfDay, newMinutes)), hour, minutes, true).show();
    }

    public interface Callback {
        void onDateTimeSelected(long unixtime);
    }

    public static class Builder {

        private final Context context;
        private Callback callback;
        private long time;

        public Builder(Context context) {
            this.context = context;
            time = System.currentTimeMillis();
        }

        public Builder setTime(long unixtime) {
            time = unixtime * 1000;
            return this;
        }

        public Builder setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }

        public void show() {
            new DateTimePicker(this).show();
        }
    }
}
