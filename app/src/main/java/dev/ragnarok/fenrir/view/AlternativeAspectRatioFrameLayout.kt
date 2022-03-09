package dev.ragnarok.fenrir.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import dev.ragnarok.fenrir.R

class AlternativeAspectRatioFrameLayout : FrameLayout {
    private var mAspectRatioWidth = 16
    private var mAspectRatioHeight = 9

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.AlternativeAspectRatioFrameLayout)
        mAspectRatioWidth =
            a.getInt(R.styleable.AlternativeAspectRatioFrameLayout_altAspectRatioWidth, 16)
        mAspectRatioHeight =
            a.getInt(R.styleable.AlternativeAspectRatioFrameLayout_altAspectRatioHeight, 9)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val originalWidth = MeasureSpec.getSize(widthMeasureSpec)
        val originalHeight = MeasureSpec.getSize(heightMeasureSpec)
        val calculatedHeight = originalWidth * mAspectRatioHeight / mAspectRatioWidth
        val finalWidth: Int
        val finalHeight: Int
        if (calculatedHeight > originalHeight) {
            finalWidth = originalHeight * mAspectRatioWidth / mAspectRatioHeight
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

    fun setAspectRatio(w: Int, h: Int) {
        mAspectRatioWidth = w
        mAspectRatioHeight = h

        // force re-calculating the layout dimension and the redraw of the view
        requestLayout()
        invalidate()
    }
}