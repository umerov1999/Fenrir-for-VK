package dev.ragnarok.fenrir.link.internal

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.core.util.PatternsCompat
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.settings.Settings

object FeedbackLinkSpanFactory {
    private val MENTIONS_PATTERN: Regex =
        Regex("\\[((?:id|club|event|public)\\d+)\\|([^]]+)]")
    val MENTIONS_AVATAR_PATTERN: Regex =
        Regex("\\[(id|club|event|public)(\\d+)\\|([^]]+)]")
    private val PHONE_NUMBER_PATTERN: Regex =
        Regex("^(?:\\+7|7|8)\\s?\\(?\\d{3}\\)?[\\s-]?\\d{3}[\\s-]?\\d{2}[\\s-]?\\d{2}$")

    @SuppressLint("RestrictedApi")
    private var URL_PATTERN: Regex = PatternsCompat.AUTOLINK_WEB_URL.toRegex()
    private var REPLY_URL_PATTERN: Regex = Regex("\\[(${URL_PATTERN.pattern})\\|([^]]+)]")

    fun parseLinks(context: Context, text: CharSequence): SpannableStringBuilder {
        var spannableStringBuilder = SpannableStringBuilder(text)

        try {
            var res: MatchResult?
            do {
                res = REPLY_URL_PATTERN.find(spannableStringBuilder)
                if (res == null) {
                    continue
                }
                val jj = res.groupValues.getOrNull(1) ?: continue

                val linkSpan = LinkSpan(context, jj, true)
                spannableStringBuilder.replace(
                    res.range.first,
                    (res.range.last + 1),
                    res.groupValues.getOrNull(16)
                )
                spannableStringBuilder.setSpan(
                    linkSpan,
                    res.range.first,
                    res.range.first + (res.groupValues.getOrNull(16)?.length.orZero()),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } while (res != null)
        } catch (_: Exception) {
        }
        try {
            val res = URL_PATTERN.findAll(spannableStringBuilder)
            for (i in res) {
                spannableStringBuilder.setSpan(
                    LinkSpan(context, i.groupValues.getOrNull(0).orEmpty(), true),
                    i.range.first,
                    (i.range.last + 1),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        } catch (_: Exception) {
        }

        try {
            val res = PHONE_NUMBER_PATTERN.findAll(spannableStringBuilder)
            for (i in res) {
                spannableStringBuilder.setSpan(
                    LinkSpan(context, "tel:" + i.groupValues.getOrNull(0).orEmpty(), false),
                    i.range.first,
                    (i.range.last + 1),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        } catch (_: Exception) {
        }

        try {
            var res: MatchResult?
            do {
                res = MENTIONS_PATTERN.find(spannableStringBuilder)
                if (res == null) {
                    continue
                }
                val linkSpan2 = LinkSpan(
                    context,
                    "https://${Settings.get().main().domain}/" + res.groupValues.getOrNull(1)
                        .orEmpty(),
                    false
                )
                val replace2 = spannableStringBuilder.replace(
                    res.range.first,
                    (res.range.last + 1),
                    res.groupValues.getOrNull(2).orEmpty()
                )
                replace2.setSpan(
                    linkSpan2,
                    res.range.first,
                    res.range.first + (res.groupValues.getOrNull(2)?.length.orZero()),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableStringBuilder = replace2
            } while (res != null)
        } catch (_: Exception) {
        }
        return spannableStringBuilder
    }
}