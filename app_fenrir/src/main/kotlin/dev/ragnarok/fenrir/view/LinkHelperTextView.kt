package dev.ragnarok.fenrir.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.text.PrecomputedText
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.net.toUri
import androidx.core.text.PrecomputedTextCompat
import androidx.core.util.PatternsCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.FragmentActivity
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.link.LinkHelper
import dev.ragnarok.fenrir.link.internal.LinkSpan
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.place.PlaceFactory.getExternalLinkPlace
import dev.ragnarok.fenrir.settings.AppPrefs
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.ClickableForegroundColorSpan
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadPoolExecutor

class LinkHelperTextView : WrapWidthTextView, ClickableForegroundColorSpan.OnHashTagClickListener {
    private var mTextStart = 0
    private var mTextLength = -1
    private var mAdditionalHashTagChars: MutableList<Char>? = null
    private var mOnHashTagClickListener: OnHashTagClickListener? = null
    private var mDisplayHashTags = false
    private var mHashTagWordColor = 0
    private var linksResolved: CharSequence? = null
    private val mResolveLinks: CancelableJob
    private var interceptSpans = false
    private var isObjectConstructed: Boolean? = null

    constructor(context: Context) : super(context) {
        mResolveLinks = CancelableJob()
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    ) {
        mResolveLinks = CancelableJob()
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        mResolveLinks = CancelableJob()
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        mAdditionalHashTagChars = ArrayList(2)
        mAdditionalHashTagChars?.add('_')
        mAdditionalHashTagChars?.add('@')
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.LinkHelperTextView)
        try {
            mTextStart = a.getInteger(R.styleable.LinkHelperTextView_linkHelperTextStart, 0)
            mTextLength = a.getInteger(R.styleable.LinkHelperTextView_linkHelperTextLength, -1)
            mHashTagWordColor = a.getColor(R.styleable.LinkHelperTextView_hashTagColor, Color.BLUE)
            mDisplayHashTags = a.getBoolean(R.styleable.LinkHelperTextView_displayHashTags, false)
        } finally {
            a.recycle()
        }
        isObjectConstructed = true

        val fontSize = Settings.get().main().fontSize
        if (Settings.get().main().fontSizeOnlyForChatsAndMessages && fontSize != 0) {
            setTextSize(0, textSize + Utils.dp(0.6f) * fontSize)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (interceptSpans && event != null && event.action == MotionEvent.ACTION_UP) {
            if (text is Spannable) {
                val tmpText = text as Spannable
                var x = event.x.toInt()
                var y = event.y.toInt()

                x -= totalPaddingLeft
                y -= totalPaddingTop

                x += scrollX
                y += scrollY

                val line = layout.getLineForVertical(y)
                val offset = layout.getOffsetForHorizontal(line, x.toFloat())

                val spans = tmpText.getSpans(offset, offset, ClickableSpan::class.java)

                if (spans.isNotEmpty()) {
                    // Клик по Spannable
                    spans[0].onClick(this)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun setColorsToAllHashTags(text: Spannable): Boolean {
        var ret = false
        var startIndexOfNextHashSign: Int
        var index = 0
        while (index < text.length - 1) {
            val sign = text[index]
            var nextNotLetterDigitCharIndex =
                index + 1 // we assume it is next. if if was not changed by findNextValidHashTagChar then index will be incremented by 1
            if (sign == '#') {
                if (!ret) {
                    ret = true
                }
                startIndexOfNextHashSign = index
                nextNotLetterDigitCharIndex =
                    findNextValidHashTagChar(text, startIndexOfNextHashSign)
                setColorForHashTagToTheEnd(
                    text,
                    startIndexOfNextHashSign,
                    nextNotLetterDigitCharIndex
                )
            }
            index = nextNotLetterDigitCharIndex
        }
        return ret
    }

    private fun findNextValidHashTagChar(text: CharSequence, start: Int): Int {
        var nonLetterDigitCharIndex = -1 // skip first sign '#"
        for (index in start + 1 until text.length) {
            val sign = text[index]
            val isValidSign =
                Character.isLetterOrDigit(sign) || mAdditionalHashTagChars?.contains(sign) == true
            if (!isValidSign) {
                nonLetterDigitCharIndex = index
                break
            }
        }
        if (nonLetterDigitCharIndex == -1) {
            // we didn't find non-letter. We are at the end of text
            nonLetterDigitCharIndex = text.length
        }
        return nonLetterDigitCharIndex
    }

    private fun setColorForHashTagToTheEnd(
        s: Spannable,
        startIndex: Int,
        nextNotLetterDigitCharIndex: Int
    ) {
        val span: CharacterStyle = if (mOnHashTagClickListener != null) {
            ClickableForegroundColorSpan(mHashTagWordColor, this)
        } else {
            // no need for clickable span because it is messing with selection when click
            ForegroundColorSpan(mHashTagWordColor)
        }
        s.setSpan(span, startIndex, nextNotLetterDigitCharIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    override fun setText(originalText: CharSequence?, type: BufferType) {
        if (isObjectConstructed != true || originalText.isNullOrEmpty() ||
            originalText is PrecomputedTextCompat ||
            Build.VERSION.SDK_INT > Build.VERSION_CODES.P && originalText is PrecomputedText
        ) {
            super.setText(originalText, type)
            return
        }
        linksResolved = originalText
        super.setText(originalText.toString(), BufferType.NORMAL)
        makeResolveLinkJob()
    }

    fun setInterceptSpans(interceptSpans: Boolean) {
        this.interceptSpans = interceptSpans
    }

    private fun linkifyUrls(spannable: Spannable): Boolean {
        var ret = false
        try {
            val res = URL_PATTERN.findAll(spannable)
            for (i in res) {
                var url = spannable.toString().substring(i.range.first, i.range.last + 1)
                if (i.range.first > 0 && spannable[i.range.first - 1] == '@') {
                    continue
                }
                if (!url.startsWith("http") && !url.startsWith("https") && !url.startsWith("rstp")) {
                    url = "https://$url"
                }
                val urlSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val openInternal = Settings.get().main().isOpenUrlInternal
                        if (openInternal >= 1 && URL_VK_PATTERN.matches(url)) {
                            LinkHelper.openUrl(
                                context as Activity,
                                Settings.get().accounts().current,
                                url
                            )
                        } else if (openInternal >= 1 && URL_YOUTUBE_PATTERN.matches(url)) {
                            val menus = ModalBottomSheetDialogFragment.Builder()
                            val hasReVanced = AppPrefs.isReVancedYoutubeInstalled(context)
                            if (hasReVanced) {
                                menus.add(
                                    OptionRequest(
                                        1,
                                        context.getString(R.string.title_play_in_youtube_vanced),
                                        R.drawable.ic_play_youtube,
                                        true
                                    )
                                )
                            }
                            menus.add(
                                OptionRequest(
                                    2,
                                    context.getString(R.string.title_play_in_newpipe),
                                    R.drawable.ic_new_pipe,
                                    true
                                )
                            )
                            if (!hasReVanced && AppPrefs.isYoutubeInstalled(context)) {
                                menus.add(
                                    OptionRequest(
                                        3,
                                        context.getString(R.string.title_play_in_youtube),
                                        R.drawable.ic_play_youtube,
                                        true
                                    )
                                )
                            }
                            menus.add(
                                OptionRequest(
                                    4,
                                    context.getString(R.string.title_play_in_another_software),
                                    R.drawable.ic_external,
                                    true
                                )
                            )
                            menus.header(
                                url,
                                R.drawable.ic_play_youtube,
                                null
                            )
                            menus.columns(1)
                            menus.show(
                                (context as FragmentActivity).supportFragmentManager,
                                "url_options"
                            ) { _, option ->
                                when (option.id) {
                                    1 -> {
                                        val intent = Intent()
                                        intent.data = url.toUri()
                                        intent.action = Intent.ACTION_VIEW
                                        intent.component = ComponentName(
                                            AppPrefs.revanced?.first.orEmpty(),
                                            AppPrefs.revanced?.second.orEmpty()
                                        )
                                        context.startActivity(intent)
                                    }

                                    2 -> {
                                        if (AppPrefs.isNewPipeInstalled(context)) {
                                            val intent = Intent()
                                            intent.data = url.toUri()
                                            intent.action = Intent.ACTION_VIEW
                                            intent.component = ComponentName(
                                                "org.schabi.newpipe",
                                                "org.schabi.newpipe.RouterActivity"
                                            )
                                            context.startActivity(intent)
                                        } else {
                                            LinkHelper.openLinkInBrowser(
                                                context,
                                                "https://github.com/TeamNewPipe/NewPipe/releases"
                                            )
                                        }
                                    }

                                    3 -> {
                                        val intent = Intent()
                                        intent.data = url.toUri()
                                        intent.action = Intent.ACTION_VIEW
                                        intent.component = ComponentName(
                                            "com.google.android.youtube",
                                            $$"com.google.android.apps.youtube.app.application.Shell$UrlActivity"
                                        )
                                        context.startActivity(intent)
                                    }

                                    4 -> {
                                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        } else if (openInternal >= 2) {
                            getExternalLinkPlace(
                                Settings.get().accounts().current,
                                url
                            ).tryOpenWith(context)
                        } else {
                            LinkHelper.openLinkInBrowser(context, url)
                        }
                    }
                }
                if (!ret) {
                    ret = true
                }
                spannable.setSpan(
                    urlSpan,
                    i.range.first,
                    i.range.last + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        } catch (_: Exception) {
        }
        return ret
    }

    private fun eraseAndColorizeAllText(text: Spannable) {
        if (getText() is Spannable) {
            val spannable = getText() as Spannable
            val spans = spannable.getSpans(0, text.length, CharacterStyle::class.java)
            for (span in spans) {
                spannable.removeSpan(span)
            }
        }
        setColorsToAllHashTags(text)
    }

    override fun onHashTagClicked(hashTag: String) {
        mOnHashTagClickListener?.onHashTagClicked(hashTag)
    }

    fun setOnHashTagClickListener(onHashTagClickListener: OnHashTagClickListener?) {
        mOnHashTagClickListener = onHashTagClickListener
    }

    fun setAdditionalHashTagChars(additionalHashTagChars: MutableList<Char>?) {
        mAdditionalHashTagChars = additionalHashTagChars
    }

    private fun getAllHashTags(withHashes: Boolean): List<String> {
        val spannable = getText() as Spannable

        // use set to exclude duplicates
        val hashTags: MutableSet<String> = LinkedHashSet()
        for (span in spannable.getSpans(0, text.length, CharacterStyle::class.java)) {
            hashTags.add(
                text.substring(
                    if (!withHashes) spannable.getSpanStart(span) + 1 else spannable.getSpanStart(
                        span
                    ), spannable.getSpanEnd(span)
                )
            )
        }
        return ArrayList(hashTags)
    }

    val allHashTags: List<String>
        get() = getAllHashTags(false)

    private fun makeResolveLinkJob() {
        mResolveLinks.cancel()
        val tmpLinksResolved = linksResolved
        if (tmpLinksResolved.isNullOrEmpty()) {
            return
        }
        mResolveLinks.set(linkResolveScheduler.launch {
            var needRefreshText = tmpLinksResolved is Spannable
            val spannable = SpannableStringBuilder.valueOf(tmpLinksResolved)
            if (mDisplayHashTags) {
                needRefreshText = setColorsToAllHashTags(spannable)
            }
            try {
                val res = PHONE_NUMBER_PATTERN.findAll(spannable)
                for (i in res) {
                    needRefreshText = true
                    spannable.setSpan(
                        LinkSpan(context, "tel:" + i.groupValues.getOrNull(0).orEmpty(), false),
                        i.range.first,
                        (i.range.last + 1),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            } catch (_: Exception) {
            }
            try {
                val res = MAIL_PATTERN.findAll(spannable)
                for (i in res) {
                    needRefreshText = true
                    spannable.setSpan(
                        LinkSpan(context, "mailto:" + i.groupValues.getOrNull(0).orEmpty(), false),
                        i.range.first,
                        (i.range.last + 1),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            } catch (_: Exception) {
            }
            if (linkifyUrls(spannable)) {
                needRefreshText = true
            }
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val textClassifier = context.getSystemService(TextClassificationManager::class.java)
                    .textClassifier
                val request = TextLinks.Request.Builder(spannable)
                textClassifier.generateLinks(request.build()).apply(
                    spannable,
                    TextLinks.APPLY_STRATEGY_IGNORE,
                    null
                )
            }
            LinkifyCompat.addLinks(
                spannable,
                Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES
            )
             */
            if (!needRefreshText) {
                linksResolved = null
                return@launch
            }
            val params = TextViewCompat.getTextMetricsParams(this@LinkHelperTextView)
            val precomputedText = PrecomputedTextCompat.create(spannable, params)
            CoroutinesUtils.inMainThread {
                linksResolved = null
                setPrecomputedText(precomputedText)
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode || linksResolved.isNullOrEmpty()) {
            return
        }
        if (isObjectConstructed == true && !mResolveLinks.hasJob()) {
            makeResolveLinkJob()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (isInEditMode) {
            return
        }
        if (isObjectConstructed == true) {
            mResolveLinks.cancel()
        }
    }

    interface OnHashTagClickListener {
        fun onHashTagClicked(hashTag: String)
    }

    companion object {
        private val URL_VK_PATTERN: Regex =
            Regex("(((http|https|rstp)://)?(\\w+.)?vk\\.(ru|com|me|cc)/\\S*)")
        private val URL_YOUTUBE_PATTERN: Regex =
            Regex("(((http|https|rstp)://)?(\\w+.)?(youtube\\.com|youtu\\.be)/\\S*)")
        private val PHONE_NUMBER_PATTERN: Regex =
            Regex("^(?:\\+7|7|8)\\s?\\(?\\d{3}\\)?[\\s-]?\\d{3}[\\s-]?\\d{2}[\\s-]?\\d{2}$")
        private val MAIL_PATTERN: Regex = PatternsCompat.EMAIL_ADDRESS.toRegex()

        @SuppressLint("RestrictedApi")
        private var URL_PATTERN: Regex = PatternsCompat.AUTOLINK_WEB_URL.toRegex()

        private val linkResolveScheduler =
            CoroutineScope(
                ScheduledThreadPoolExecutor(
                    2,
                    ThreadPoolExecutor.DiscardPolicy()
                ).asCoroutineDispatcher()
            )
    }
}