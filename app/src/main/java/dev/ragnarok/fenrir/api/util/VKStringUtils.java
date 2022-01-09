package dev.ragnarok.fenrir.api.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for join collections to strings
 */
public class VKStringUtils {

    private static final String pattern_string_profile_id = "^(id)?(\\d{1,10})$";
    private static final Pattern pattern_profile_id = Pattern.compile(pattern_string_profile_id);

    /**
     * Returns true if the string is null or 0-length.
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param tokens an array objects to be joined. Strings will be formed from
     *               the objects by calling object.toString().
     */
    public static String join(CharSequence delimiter, Object[] tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }

        return sb.toString();
    }

    public static String firstNonEmptyString(String... array) {
        for (String s : array) {
            if (s != null && s.length() > 0) {
                return s;
            }
        }

        return null;
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param tokens an array objects to be joined. Strings will be formed from
     *               the objects by calling object.toString().
     */
    public static String join(CharSequence delimiter, Iterable<?> tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }

    public static String extractPattern(String string, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(string);
        if (!m.find()) {
            return null;
        }

        return m.toMatchResult().group(1);
    }

    public static String unescape(String text) {
        if (text == null) {
            return null;
        }

        return text.replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("<br>", "\n")
                .replace("&gt;", ">")
                .replace("&lt;", "<")
                .replace("<br/>", "\n")
                .replace("&ndash;", "-")
                .trim();

        //Баг в API
        //amp встречается в сообщении, br в Ответах тип comment_photo, gt lt на стене - баг API, ndash в статусе когда аудио транслируется
        //quot в тексте сообщения из LongPoll - то есть в уведомлении
    }

    public static String parseProfileId(String text) {
        Matcher m = pattern_profile_id.matcher(text);
        if (!m.find()) {
            return null;
        }

        return m.group(2);
    }
}
