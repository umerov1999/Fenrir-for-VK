package dev.ragnarok.fenrir.util;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import dev.ragnarok.fenrir.R;

public class AppTextUtils {
    public static final long ONE_DAY_SEC = 24 * 60 * 60;
    private static final Date DATE = new Date();
    private static final Calendar CALENDAR = Calendar.getInstance();
    private static final String K = "K";
    private static final String ZERO = "0";
    private static final String TWO_ZERO = "00";
    private static final String POINT = ".";
    private static final String EMPTY = "";
    private static SimpleDateFormat SHORT_DATE = new SimpleDateFormat("HH:mm", Utils.getAppLocale());
    private static SimpleDateFormat FULL_DATE = new SimpleDateFormat("dd MMM yyyy HH:mm", Utils.getAppLocale());
    private static SimpleDateFormat FULL_LITTLE_DATE = new SimpleDateFormat("dd.MM.yy HH:mm", Utils.getAppLocale());

    public static void updateDateLang(Locale locale) {
        SHORT_DATE = new SimpleDateFormat("HH:mm", locale);
        FULL_DATE = new SimpleDateFormat("dd MMM yyyy HH:mm", locale);
        FULL_LITTLE_DATE = new SimpleDateFormat("dd.MM.yy HH:mm", locale);
    }

    public static String safeTrim(String text, @Nullable String ifNull) {
        return text == null ? ifNull : text.trim();
    }

    public static String reduceStringForPost(String input) {
        return reduceText(input, 400);
    }

    public static String reduceText(String text, int maxLenght) {
        if (text == null || text.length() < maxLenght) {
            return text;
        } else {
            return text.substring(0, maxLenght).concat("...");
        }
    }

    /**
     * Получения строки с размером в мегабайтах
     *
     * @param size размер в байтах
     * @return строка типа "13.6 Mb"
     */
    public static String getSizeString(long size) {
        double sizeDouble = ((double) size) / 1024 / 1024;
        double newDouble = new BigDecimal(sizeDouble).setScale(2, RoundingMode.UP).doubleValue();
        return newDouble + " Mb";
    }

    /**
     * Получение строки с датой и временем сообщений
     *
     * @param unixTime дата в формате unix-time
     * @return строка с датой и временем
     */
    public static String getDateFromUnixTime(long unixTime) {
        CALENDAR.setTimeInMillis(System.currentTimeMillis());

        DATE.setTime(unixTime * 1000);
        CALENDAR.set(CALENDAR.get(Calendar.YEAR), CALENDAR.get(Calendar.MONTH), CALENDAR.get(Calendar.DATE), 0, 0, 0);
        return unixTime * 1000 > CALENDAR.getTimeInMillis() ? SHORT_DATE.format(DATE) : FULL_DATE.format(DATE);
    }

    /**
     * Получение строки с датой и временем сообщений
     *
     * @param unixTime дата в формате unix-time
     * @return строка с датой и временем
     */
    public static String getDateFromUnixTime(Context context, long unixTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);

        DATE.setTime(unixTime * 1000);

        long startToday = calendar.getTimeInMillis() / 1000;
        long startTomorrow = startToday + ONE_DAY_SEC;
        long startOfDayAfterTomorrow = startTomorrow + ONE_DAY_SEC;
        long startOfYesterday = startToday - ONE_DAY_SEC;

        if (unixTime >= startToday && unixTime < startTomorrow) {
            return context.getString(R.string.formatted_date_today, SHORT_DATE.format(DATE));
        }

        if (unixTime >= startOfYesterday && unixTime < startToday) {
            return context.getString(R.string.formatted_date_yesterday, SHORT_DATE.format(DATE));
        }

        if (unixTime >= startTomorrow && unixTime < startOfDayAfterTomorrow) {
            return context.getString(R.string.formatted_date_tomorrow, SHORT_DATE.format(DATE));
        }

        return FULL_DATE.format(DATE);
    }

    /**
     * Получение строки с датой и временем сообщений
     *
     * @param unixTime дата в формате unix-time
     * @return строка с датой и временем
     */
    public static String getDateFromUnixTimeShorted(Context context, long unixTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);

        DATE.setTime(unixTime * 1000);

        long startToday = calendar.getTimeInMillis() / 1000;
        long startTomorrow = startToday + ONE_DAY_SEC;
        long startOfDayAfterTomorrow = startTomorrow + ONE_DAY_SEC;
        long startOfYesterday = startToday - ONE_DAY_SEC;

        if (unixTime >= startToday && unixTime < startTomorrow) {
            return SHORT_DATE.format(DATE);
        }

        if (unixTime >= startOfYesterday && unixTime < startToday) {
            return context.getString(R.string.formatted_date_yesterday_clean, SHORT_DATE.format(DATE));
        }

        if (unixTime >= startTomorrow && unixTime < startOfDayAfterTomorrow) {
            return context.getString(R.string.formatted_date_tomorrow_clean, SHORT_DATE.format(DATE));
        }

        return FULL_LITTLE_DATE.format(DATE);
    }

    public static String getDurationString(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        if (hours == 0) {
            return twoDigitString(minutes) + ":" + twoDigitString(seconds);
        } else {
            return twoDigitString(hours) + ":" + twoDigitString(minutes) + ":" + twoDigitString(seconds);
        }
    }

    public static String getDurationStringMS(int ms) {
        return getDurationString(ms / 1000);
    }

    private static String twoDigitString(int number) {
        if (number == 0) {
            return TWO_ZERO;
        }

        if (number / 10 == 0) {
            return ZERO + number;
        }

        return String.valueOf(number);
    }

    public static String getCounterWithK(int counter) {
        int num = counter / 1000;
        if (num >= 1 && num < 10) {
            //return num + "." + (counter / 100) % 10 + K;
            return String.valueOf(counter);
        }
        if (num >= 10) {
            return num + K;
        }

        return String.valueOf(counter);
    }

    public static String getDateWithZeros(String date) {
        if (TextUtils.isEmpty(date) || date.equals(ZERO)) {
            return EMPTY;
        }

        String[] tmp = date.split(Pattern.quote(POINT));
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].length() == 1) {
                tmp[i] = ZERO + tmp[i];
            }
        }

        return TextUtils.join(POINT, tmp);
    }
}
