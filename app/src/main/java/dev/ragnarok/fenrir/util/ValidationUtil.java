package dev.ragnarok.fenrir.util;

import static dev.ragnarok.fenrir.util.Utils.trimmedIsEmpty;

import android.annotation.SuppressLint;

import androidx.core.util.PatternsCompat;

public class ValidationUtil {

    @SuppressLint("RestrictedApi")
    public static boolean isValidURL(String url) {
        return url != null && PatternsCompat.AUTOLINK_WEB_URL.matcher(url).find();
    }

    public static boolean isValidIpAddress(String ipv4) {
        if (trimmedIsEmpty(ipv4)) {
            return false;
        }

        ipv4 = ipv4.trim();

        String[] blocks = ipv4.split("\\.");

        if (blocks.length != 4) {
            return false;
        }

        for (String block : blocks) {
            try {
                int num = Integer.parseInt(block);

                if (num > 255 || num < 0) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }
}