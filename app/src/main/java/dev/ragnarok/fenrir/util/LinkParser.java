package dev.ragnarok.fenrir.util;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.Patterns;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkParser {
    public static final Pattern MENTIONS_PATTERN = Pattern.compile("\\[((?:id|club|event|public)[0-9]+)\\|([^]]+)]");
    public static final Pattern MENTIONS_AVATAR_PATTERN = Pattern.compile("\\[((?:id|club|event|public))([0-9]+)\\|([^]]+)]");
    public static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("\\+\\d{8,15}");
    public static Pattern REPLY_URL_PATTERN;
    public static Pattern URL_PATTERN;

    static {
        URL_PATTERN = null;
        REPLY_URL_PATTERN = null;
        try {
            URL_PATTERN = Pattern.compile("((?:(http|https|Http|Https|ftp|Ftp)://(?:(?:[a-zA-Z0-9$\\-_.+!*'(),;?&=]|(?:%[a-fA-F0-9]{2})){1,64}(?::(?:[a-zA-Z0-9$\\-_.+!*'(),;?&=]|(?:%[a-fA-F0-9]{2})){1,25})?@)?)?(?:" + Pattern.compile("(([a-zA-Z0-9 -퟿豈-﷏ﷰ-￯]([a-zA-Z0-9 -퟿豈-﷏ﷰ-￯\\-]{0,61}[a-zA-Z0-9 -퟿豈-﷏ﷰ-￯])?\\.)+([a-zA-Z0-9-]{2,63}|рф|бел|укр)|" + Patterns.IP_ADDRESS + ")") + ")" + "(?::\\d{1,5})?)" + "(/(?:(?:[" + "a-zA-Z0-9 -퟿豈-﷏ﷰ-￯" + ";/?:@&=#~" + "\\-.+!*'(),_])|(?:%[a-fA-F0-9]{2}))*)?" + "(?:\\b|$)");
            REPLY_URL_PATTERN = Pattern.compile("\\[(" + URL_PATTERN + ")\\|([^]]+)]");
        } catch (Exception ignored) {
        }
    }

    public static boolean isNumber(String str) {
        return str != null && str.matches("\\d+");
    }

    public static SpannableStringBuilder parseLinks(Context context, CharSequence charSequence) {
        SpannableStringBuilder spannableStringBuilder;
        CharSequence r12;
        SpannableStringBuilder spannableStringBuilder2 = new SpannableStringBuilder(charSequence);

        Matcher matcher = REPLY_URL_PATTERN.matcher(charSequence);
        SpannableStringBuilder spannableStringBuilder3 = spannableStringBuilder2;
        int i2 = 0;
        while (matcher.find()) {
            LinkSpan linkSpan = new LinkSpan(context, matcher.group(1), true);
            SpannableStringBuilder replace = spannableStringBuilder3.replace(matcher.start() - i2, matcher.end() - i2, matcher.group(14));
            replace.setSpan(linkSpan, matcher.start() - i2, (matcher.start() - i2) + matcher.group(14).length(), 0);
            i2 += matcher.group().length() - Objects.requireNonNull(matcher.group(14)).length();
            spannableStringBuilder3 = replace;
        }
        r12 = spannableStringBuilder3;
        spannableStringBuilder = spannableStringBuilder3;


        Matcher matcher2 = URL_PATTERN.matcher(r12);
        while (matcher2.find()) {
            if (!isNumber(matcher2.group(6)) && (matcher2.start() <= 0 || spannableStringBuilder.charAt(matcher2.start() - 1) != '@')) {
                spannableStringBuilder.setSpan(new LinkSpan(context, matcher2.group(), true), matcher2.start(), matcher2.end(), 0);
            }
        }

        Matcher matcher3 = PHONE_NUMBER_PATTERN.matcher(r12);
        while (matcher3.find()) {
            spannableStringBuilder.setSpan(new LinkSpan(context, "tel:" + matcher3.group(), false), matcher3.start(), matcher3.end(), 0);
        }
        Matcher matcher5 = MENTIONS_PATTERN.matcher(r12);
        SpannableStringBuilder spannableStringBuilder4 = spannableStringBuilder;
        int i3 = 0;
        while (matcher5.find()) {
            LinkSpan linkSpan2 = new LinkSpan(context, "https://vk.com/" + matcher5.group(1), false);
            SpannableStringBuilder replace2 = spannableStringBuilder4.replace(matcher5.start() - i3, matcher5.end() - i3, matcher5.group(2));
            replace2.setSpan(linkSpan2, matcher5.start() - i3, (matcher5.start() - i3) + matcher5.group(2).length(), 0);
            i3 = (matcher5.group().length() - Objects.requireNonNull(matcher5.group(2)).length()) + i3;
            spannableStringBuilder4 = replace2;
        }
        return spannableStringBuilder4;
    }
}
