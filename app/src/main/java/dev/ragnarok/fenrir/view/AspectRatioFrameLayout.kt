package dev.ragnarok.fenrir.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import dev.ragnarok.fenrir.R

class AspectRatioFrameLayout : FrameLayout {
    private var mProportionWidth = 0
    private var mProportionHeight = 0

    constructor(context: Context) : super(context) {
        mProportionWidth = DEFAULT_PROPORTION_WIDTH
        mProportionHeight = DEFAULT_PROPORTION_HEIGHT
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.AspectRatioFrameLayout,
            0, 0
        )
        try {
            mProportionWidth = a.getInt(
                R.styleable.AspectRatioFrameLayout_aspectRatioWidth,
                DEFAULT_PROPORTION_WIDTH
            )
            mProportionHeight = a.getInt(
                R.styleable.AspectRatioFrameLayout_aspectRatioHeight,
                DEFAULT_PROPORTION_HEIGHT
            )
        } finally {
            a.recycle()
        }
    }

    fun setAspectRatio(w: Int, h: Int) {
        mProportionHeight = h
        mProportionWidth = w
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val originalWidth = MeasureSpec.getSize(widthMeasureSpec)
        val originalHeight = MeasureSpec.getSize(heightMeasureSpec)
        val calculatedHeight = originalWidth * mProportionHeight / mProportionWidth
        val finalWidth: Int
        val finalHeight: Int
        if (calculatedHeight > originalHeight) {
            finalWidth = originalHeight * mProportionWidth / mProportionHeight
            finalHeight = originalHeight
        } else {
            finalWidth = originalWidth
            finalHeight = calculatedHeight
        }
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY)
        )
    }

    companion object {
        private const val DEFAULT_PROPORTION_WIDTH = 16
        private const val DEFAULT_PROPORTION_HEIGHT = 9
    }
}