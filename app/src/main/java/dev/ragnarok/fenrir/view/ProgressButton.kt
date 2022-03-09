package dev.ragnarok.fenrir.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import dev.ragnarok.fenrir.R

class ProgressButton : FrameLayout {
    private var mProgressIcon: ImageView? = null
    private var mTitleRoot: View? = null
    private var mTitleTextView: TextView? = null
    private var mProgressNow = false

    constructor(context: Context) : super(context) {
        init(context, null)
    }

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
        val a = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton)
        val layout: Int
        val buttonColor: Int
        val buttonTitle: String?
        val allCaps: Boolean
        try {
            layout = a.getResourceId(
                R.styleable.ProgressButton_button_layout,
                R.layout.content_progress_button
            )
            buttonColor = a.getColor(R.styleable.ProgressButton_button_color, Color.BLUE)
            buttonTitle = a.getString(R.styleable.ProgressButton_button_text)
            allCaps = a.getBoolean(R.styleable.ProgressButton_button_all_caps, true)
        } finally {
            a.recycle()
        }
        val view = LayoutInflater.from(context).inflate(layout, this, false)
        view.setBackgroundColor(buttonColor)
        mTitleTextView = view.findViewById(R.id.progress_button_title_text)
        mTitleTextView?.text = buttonTitle
        mTitleTextView?.isAllCaps = allCaps
        mProgressIcon = view.findViewById(R.id.progress_button_progress_icon)
        mTitleRoot = view.findViewById(R.id.progress_button_title_root)
        addView(view)
        resolveViews()
    }

    fun setText(charSequence: CharSequence?) {
        mTitleTextView?.text = charSequence
    }

    private fun resolveViews() {
        mProgressIcon?.visibility =
            if (mProgressNow) VISIBLE else INVISIBLE
        mTitleRoot?.visibility =
            if (mProgressNow) INVISIBLE else VISIBLE
        if (mProgressNow) {
            val animation = AnimationUtils.loadAnimation(context, R.anim.anim_button_progress)
            mProgressIcon?.startAnimation(animation)
        } else {
            mProgressIcon?.clearAnimation()
        }
    }

    fun changeState(progress: Boolean) {
        mProgressNow = progress
        resolveViews()
    }
}