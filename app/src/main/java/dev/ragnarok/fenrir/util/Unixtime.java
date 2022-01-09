package dev.ragnarok.fenrir.util;

import java.util.Calendar;

public class Unixtime {

    public static long now() {
        return System.currentTimeMillis() / 1000;
    }

    public static long of(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return calendar.getTimeInMillis() / 1000;
    }
}
