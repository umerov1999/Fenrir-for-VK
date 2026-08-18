package dev.ragnarok.fenrir.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.core.text.PrecomputedTextCompat
import androidx.core.util.PatternsCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.FragmentActivity
import dev.ragnarok.fenrir.Constants
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
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromScopeToMain
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadPoolExecutor

class LinkHelperTextView : WrapWidthTextView, ClickableForegroundColorSpan.OnHashTagClickListener {
    private var mTextStart = 0
    private var mTextLength = -1
    private var mAdditionalHashTagChars: MutableList<Char>? = null
    private var mOnHashTagClickListener: OnHashTagClickListener? = null
    private var mDisplayHashTags = false
    private var mHashTagWordColor = 0
    private var linksResolverTaskData: CharSequence? = null
    private var mResolveLinks: CancelableJob? = null
    private var interceptSpans = false

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    ) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        mResolveLinks = CancelableJob()

        mAdditionalHashTagChars = ArrayList(2)
        mAdditionalHashTagChars?.add('_')
        mAdditionalHashTagChars?.add('@')
        var fontScaleFactor: Float
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.LinkHelperTextView)
        try {
            mTextStart = a.getInteger(R.styleable.LinkHelperTextView_linkHelperTextStart, 0)
            mTextLength = a.getInteger(R.styleable.LinkHelperTextView_linkHelperTextLength, -1)
            mHashTagWordColor = a.getColor(R.styleable.LinkHelperTextView_hashTagColor, Color.BLUE)
            mDisplayHashTags = a.getBoolean(R.styleable.LinkHelperTextView_displayHashTags, false)
            fontScaleFactor = a.getFloat(R.styleable.LinkHelperTextView_fontScaleFactor, 0.6f)
        } finally {
            a.recycle()
        }

        val fontSize = Settings.get().main().fontSize
        if (Settings.get()
                .main().fontSizeOnlyForChatsAndMessages && fontSize != 0 && fontScaleFactor > 0
        ) {
            setTextSize(0, textSize + Utils.dp(fontScaleFactor) * fontSize)
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
                index + 1 // we assume it is next. if was not changed by findNextValidHashTagChar then index will be incremented by 1
            if (sign == '#') {
                if (!ret) {
                    ret = true
                }
                startIndexOfNextHashSign = index
                nextNotLetterDigitCharIndex =
                    findNextValidHashTagChar(text, startIndexOfNextHashSign)
                if (startIndexOfNextHashSign + 1 == nextNotLetterDigitCharIndex) {
                    index++
                    continue
                }
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
        var nonLetterDigitCharIndex = -1 // skip first sign '#'
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

    fun precompute(@StringRes res: Int) {
        precompute(context.getString(res))
    }

    fun precompute(text: CharSequence?) {
        mResolveLinks?.cancel()
        linksResolverTaskData = null
        if (text != null) {
            setText(text.toString(), BufferType.NORMAL)
        } else {
            setText(null, BufferType.NORMAL)
        }
        if (!isTextSelectable) {
            linksResolverTaskData = text
            makeResolveLinkJob()
        }
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

    fun resolveLinkFlow(
        textWithLink: CharSequence,
        metrics: PrecomputedTextCompat.Params
    ): Flow<PrecomputedTextCompat?> {
        return flow {
            var needRefreshText = textWithLink is Spannable
            val spannable = SpannableStringBuilder.valueOf(textWithLink)
            if (mDisplayHashTags && setColorsToAllHashTags(spannable)) {
                needRefreshText = true
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
            } catch (e: Exception) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace()
                }
                if (e is CancellationException) {
                    throw e
                }
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
            } catch (e: Exception) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace()
                }
                if (e is CancellationException) {
                    throw e
                }
            }
            if (linkifyUrls(spannable)) {
                needRefreshText = true
            }
            emit(
                if (!needRefreshText) {
                    null
                } else {
                    PrecomputedTextCompat.create(spannable, metrics)
                }
            )
        }
    }

    private fun makeResolveLinkJob() {
        val tmpLinksResolved = linksResolverTaskData
        if (tmpLinksResolved.isNullOrEmpty()) {
            return
        }

        mResolveLinks?.set(
            resolveLinkFlow(
                tmpLinksResolved,
                TextViewCompat.getTextMetricsParams(this)
            ).fromScopeToMain(linkResolveScheduler, { txt ->
                linksResolverTaskData = null
                if (txt != null) {
                    try {
                        setPrecomputedText(txt)
                    } catch (e: Exception) {
                        if (Constants.IS_DEBUG) {
                            e.printStackTrace()
                        }
                    }
                }
            }, {
                linksResolverTaskData = null
                if (Constants.IS_DEBUG) {
                    it.printStackTrace()
                }
            })
        )
    }

    fun makeTextSelectable(selectable: Boolean) {
        mResolveLinks?.cancel()
        linksResolverTaskData = null
        if (text != null) {
            setText(text.toString(), BufferType.NORMAL)
        } else {
            setText(null, BufferType.NORMAL)
        }
        setTextIsSelectable(selectable)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode || linksResolverTaskData.isNullOrEmpty()) {
            return
        }
        mResolveLinks?.cancel()
        makeResolveLinkJob()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (isInEditMode) {
            return
        }
        mResolveLinks?.cancel()
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