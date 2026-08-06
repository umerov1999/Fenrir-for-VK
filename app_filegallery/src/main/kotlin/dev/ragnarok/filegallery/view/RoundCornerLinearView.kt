package dev.ragnarok.filegallery.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import dev.ragnarok.filegallery.R
import dev.ragnarok.filegallery.util.Utils

class RoundCornerLinearView : LinearLayout {
    private val VIEW_PAINT = Paint(Paint.ANTI_ALIAS_FLAG)
    private val PATH = Path()
    val DEFAULT_RADIUS = 12f
    val DEFAULT_FILL_COLOR = Color.RED
    val DEFAULT_STROKE_COLOR = Color.BLACK
    val DEFAULT_FILL_ALPHA = 255
    val DEFAULT_STROKE_ALPHA = 255
    val DEFAULT_IS_STROKE = false
    val DEFAULT_STROKE_WIDTH = 1f
    private var radius_top_left = DEFAULT_RADIUS
    private var radius_top_right = DEFAULT_RADIUS
    private var radius_bottom_left = DEFAULT_RADIUS
    private var radius_bottom_right = DEFAULT_RADIUS
    private var fillColor = DEFAULT_FILL_COLOR
    private var strokeColor = DEFAULT_STROKE_COLOR
    private var fillAlpha = DEFAULT_FILL_ALPHA
    private var strokeAlpha = DEFAULT_STROKE_ALPHA
    private var isStroke = DEFAULT_IS_STROKE
    private var strokeWidth = DEFAULT_STROKE_WIDTH
    private var excludeStrokeTop = false
    private var excludeStrokeBottom = false
    private var excludeStrokeLeft = false
    private var excludeStrokeRight = false

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setWillNotDraw(false)
        VIEW_PAINT.isDither = true
        VIEW_PAINT.isAntiAlias = true
        initializeAttributes(context, attrs)
    }

    private fun dp2px(dpValue: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue,
            resources.displayMetrics
        )
    }

    private fun initializeAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.RoundCornerLinearView) {
                radius_top_left = getDimension(
                    R.styleable.RoundCornerLinearView_radius_top_left,
                    dp2px(DEFAULT_RADIUS)
                )
                radius_top_right = getDimension(
                    R.styleable.RoundCornerLinearView_radius_top_right,
                    dp2px(DEFAULT_RADIUS)
                )
                radius_bottom_left = getDimension(
                    R.styleable.RoundCornerLinearView_radius_bottom_left,
                    dp2px(DEFAULT_RADIUS)
                )
                radius_bottom_right = getDimension(
                    R.styleable.RoundCornerLinearView_radius_bottom_right,
                    dp2px(DEFAULT_RADIUS)
                )
                fillColor =
                    getColor(R.styleable.RoundCornerLinearView_fill_color, DEFAULT_FILL_COLOR)
                strokeColor =
                    getColor(
                        R.styleable.RoundCornerLinearView_view_stroke_color,
                        DEFAULT_STROKE_COLOR
                    )
                fillAlpha =
                    getInt(R.styleable.RoundCornerLinearView_fill_alpha, DEFAULT_FILL_ALPHA)
                strokeAlpha =
                    getInt(R.styleable.RoundCornerLinearView_stroke_alpha, DEFAULT_STROKE_ALPHA)
                isStroke = getBoolean(
                    R.styleable.RoundCornerLinearView_view_is_stroke,
                    DEFAULT_IS_STROKE
                )
                strokeWidth = getDimension(
                    R.styleable.RoundCornerLinearView_view_stroke_width,
                    dp2px(DEFAULT_STROKE_WIDTH)
                )

                excludeStrokeTop = getBoolean(
                    R.styleable.RoundCornerLinearView_stroke_exclude_top,
                    false
                )
                excludeStrokeBottom = getBoolean(
                    R.styleable.RoundCornerLinearView_stroke_exclude_bottom,
                    false
                )
                excludeStrokeLeft = getBoolean(
                    R.styleable.RoundCornerLinearView_stroke_exclude_left,
                    false
                )
                excludeStrokeRight = getBoolean(
                    R.styleable.RoundCornerLinearView_stroke_exclude_right,
                    false
                )
            }
        }
    }

    fun setFillColor(@ColorInt fillColor: Int) {
        this.fillColor = fillColor
        invalidate()
    }

    fun setFillAlpha(fillAlpha: Int) {
        this.fillAlpha = fillAlpha
        invalidate()
    }

    fun setStrokeColor(@ColorInt strokeColor: Int) {
        this.strokeColor = strokeColor
        invalidate()
    }

    fun setStrokeAlpha(strokeAlpha: Int) {
        this.strokeAlpha = strokeAlpha
        invalidate()
    }

    fun setStrokeWidth(strokeWidth: Float) {
        this.strokeWidth = strokeWidth
        invalidate()
    }

    fun setIsStroke(isStroke: Boolean) {
        this.isStroke = isStroke
        invalidate()
    }

    fun setRadiusTopLeft(radius_top_left: Float) {
        this.radius_top_left = radius_top_left
        invalidate()
    }

    fun setRadiusTopRight(radius_top_right: Float) {
        this.radius_top_right = radius_top_right
        invalidate()
    }

    fun setRadiusBottomLeft(radius_bottom_left: Float) {
        this.radius_bottom_left = radius_bottom_left
        invalidate()
    }

    fun setRadiusBottomRight(radius_bottom_right: Float) {
        this.radius_bottom_right = radius_bottom_right
        invalidate()
    }

    fun setStrokeExclude(top: Boolean, bottom: Boolean, left: Boolean, right: Boolean) {
        excludeStrokeTop = top
        excludeStrokeBottom = bottom
        excludeStrokeLeft = left
        excludeStrokeRight = right
        invalidate()
    }

    private fun doPaint(canvas: Canvas, @ColorInt color: Int, alpha: Int, isStrokeMode: Boolean) {
        val widthTmp = width - strokeWidth
        val heightTmp = height - strokeWidth
        if (widthTmp <= 0 || heightTmp <= 0) {
            return
        }
        VIEW_PAINT.color = color
        VIEW_PAINT.alpha = alpha
        VIEW_PAINT.style = if (isStrokeMode) Paint.Style.STROKE else Paint.Style.FILL
        VIEW_PAINT.strokeWidth = strokeWidth
        VIEW_PAINT.shader = null
        VIEW_PAINT.strokeCap = Paint.Cap.ROUND
        VIEW_PAINT.strokeJoin = Paint.Join.ROUND
        PATH.reset()
        PATH.fillType = Path.FillType.EVEN_ODD
        var isMove: Boolean
        if (!excludeStrokeTop || !isStrokeMode) {
            isMove = false
            PATH.moveTo(strokeWidth, radius_top_left)
            if (radius_top_left > 0f) {
                PATH.arcTo(
                    strokeWidth,
                    strokeWidth,
                    2 * radius_top_left,
                    2 * radius_top_left,
                    180f,
                    90f,
                    false
                )
            }
            PATH.lineTo(widthTmp - radius_top_right, strokeWidth)
        } else {
            isMove = true
        }
        if (!excludeStrokeRight || !isStrokeMode) {
            if (isMove) {
                isMove = false
                PATH.moveTo(widthTmp - radius_top_right, strokeWidth)
            }
            if (radius_top_right > 0f) {
                PATH.arcTo(
                    widthTmp - 2 * radius_top_right,
                    strokeWidth,
                    widthTmp,
                    2 * radius_top_right,
                    270f,
                    90f,
                    false
                )
            }
            PATH.lineTo(widthTmp, heightTmp - radius_bottom_right)
        } else {
            isMove = true
        }
        if (!excludeStrokeBottom || !isStrokeMode) {
            if (isMove) {
                isMove = false
                PATH.moveTo(widthTmp, heightTmp - radius_bottom_right)
            }
            if (radius_bottom_right > 0f) {
                PATH.arcTo(
                    widthTmp - 2 * radius_bottom_right,
                    heightTmp - 2 * radius_bottom_right,
                    widthTmp,
                    heightTmp,
                    0f,
                    90f,
                    false
                )
            }
            PATH.lineTo(strokeWidth + radius_bottom_left, heightTmp)
        } else {
            isMove = true
        }
        if (!excludeStrokeLeft || !isStrokeMode) {
            if (isMove) {
                PATH.moveTo(radius_bottom_left, heightTmp)
            }
            if (radius_bottom_left > 0f) {
                PATH.arcTo(
                    strokeWidth,
                    heightTmp - 2 * radius_bottom_left,
                    2 * radius_bottom_left,
                    heightTmp,
                    90f,
                    90f,
                    false
                )
            }
            PATH.lineTo(strokeWidth, radius_top_left)
        }
        PATH.close()
        canvas.drawPath(PATH, VIEW_PAINT)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isStroke) {
            doPaint(canvas, fillColor, fillAlpha, false)
            if (strokeWidth > Utils.dp(1f)) {
                doPaint(canvas, strokeColor, strokeAlpha, true)
            }
        } else {
            doPaint(canvas, strokeColor, strokeAlpha, true)
        }
    }
}
