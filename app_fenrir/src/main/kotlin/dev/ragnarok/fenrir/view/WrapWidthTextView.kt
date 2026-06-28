package dev.ragnarok.fenrir.view

import android.content.Context
import android.text.Layout
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView
import dev.ragnarok.fenrir.R
import kotlin.math.ceil

open class WrapWidthTextView : MaterialTextView {
    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    ) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init(context, attrs)
    }

    var fixWrapText = false
        private set

    private fun init(context: Context, attributeSet: AttributeSet?) {
        val a =
            context.theme.obtainStyledAttributes(attributeSet, R.styleable.WrapWidthTextView, 0, 0)
        fixWrapText = try {
            a.getBoolean(R.styleable.WrapWidthTextView_fixWrapText, false)
        } finally {
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (fixWrapText && layout != null && MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            val widthCalc = getMaxWidth(layout)
            if (widthCalc in 1 until measuredWidth) {
                super.onMeasure(
                    MeasureSpec.makeMeasureSpec(widthCalc, MeasureSpec.AT_MOST),
                    heightMeasureSpec
                )
            }
        }
    }

    private fun getMaxWidth(textLayout: Layout): Int {
        val linesCount = textLayout.lineCount
        if (linesCount < 2) {
            return 0
        }
        var maxWidth = 0f
        for (i in 0 until linesCount) {
            maxWidth = maxWidth.coerceAtLeast(textLayout.getLineWidth(i))
        }
        return ceil(maxWidth.toDouble()).toInt()
    }
}