package dev.ragnarok.fenrir.view.swipehelper

import android.content.Context
import android.util.AttributeSet
import android.util.Property
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.customview.widget.ViewDragHelper
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.view.TouchImageView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class VerticalSwipeBehavior<V : View> : CoordinatorLayout.Behavior<V> {

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun <V : View> from(v: V): VerticalSwipeBehavior<V> {
            val lp = v.layoutParams
            require(lp is CoordinatorLayout.LayoutParams)
            val behavior = lp.behavior
            requireNotNull(behavior)
            require(behavior is VerticalSwipeBehavior)
            return behavior as VerticalSwipeBehavior<V>
        }
    }

    var sideEffect: SideEffect = AlphaElevationSideEffect()
    var clamp: VerticalClamp = FractionClamp(1f, 1f)
    var settle: PostAction = OriginSettleAction()
    var listener: SwipeListener? = null

    @Suppress("unused")
    constructor() : super()

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private var dragHelper: ViewDragHelper? = null
    private var interceptingEvents = false

    private val callback = object : ViewDragHelper.Callback() {

        private val INVALID_POINTER_ID = -1
        private var currentPointer = INVALID_POINTER_ID
        private var originTop: Int = 0

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return (currentPointer == INVALID_POINTER_ID || pointerId == currentPointer) && (child !is TouchImageView || (!child.isZoomed && !child.canScrollVertically(
                -1
            ) && !child.canScrollVertically(1)))
        }

        override fun onViewCaptured(child: View, activePointerId: Int) {
            listener?.onCaptured()
            originTop = child.top
            currentPointer = activePointerId
            //
            sideEffect.onViewCaptured(child)
            settle.onViewCaptured(child)
            clamp.onViewCaptured(child.top)
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return clamp.constraint(child.height, top, dy)
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return child.left
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return child.height
        }

        override fun onViewReleased(child: View, xvel: Float, yvel: Float) {
            val diff = child.top - originTop
            val settled = dragHelper?.let {
                if (abs(diff) > Settings.get().ui().isPhoto_swipe_triggered_pos) {
                    settle.releasedAbove(it, diff, child)
                } else {
                    settle.releasedBelow(it, diff, child)
                }
            } ?: false
            if (settled) {
                listener?.onPreSettled(diff)
                child.postOnAnimation(RecursiveSettle(child, diff))
            } else
                listener?.onReleased()
            currentPointer = INVALID_POINTER_ID
        }

        override fun onViewPositionChanged(child: View, left: Int, top: Int, dx: Int, dy: Int) {
            val factor = if (top < originTop) {
                val diff = originTop - top
                -clamp.upCast(diff, top, child.height, dy)
            } else {
                val diff = top - originTop
                clamp.downCast(diff, top, child.height, dy)
            }
            sideEffect.apply(child, factor)
        }
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: V,
        ev: MotionEvent
    ): Boolean {
        var isIntercept = interceptingEvents
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isIntercept = parent.isPointInChildBounds(child, ev.x.toInt(), ev.y.toInt())
                interceptingEvents = isIntercept
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                interceptingEvents = false
            }
        }
        return if (isIntercept) {
            helper(parent).shouldInterceptTouchEvent(ev)
        } else false
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, ev: MotionEvent): Boolean {
        val helper = helper(parent)
        return if (helper.capturedView == child || helper.isViewUnder(
                child,
                ev.x.toInt(),
                ev.y.toInt()
            )
        ) {
            helper.processTouchEvent(ev)
            true
        } else {
            false
        }
    }

    private fun helper(parent: ViewGroup): ViewDragHelper {
        var h = dragHelper
        if (h == null) {
            h = ViewDragHelper.create(parent, callback)
            dragHelper = h
            return h
        }
        return h
    }

    private inner class RecursiveSettle(private val child: View, private val diff: Int) : Runnable {

        override fun run() {
            if (dragHelper?.continueSettling(true) == true) {
                child.postOnAnimation(this)
            } else {
                child.removeCallbacks(this)
                listener?.onPostSettled(abs(diff) > Settings.get().ui().isPhoto_swipe_triggered_pos)
            }
        }
    }

    interface SwipeListener {

        /**
         * Сalled before settle
         * @param diff passed distance
         */
        fun onPreSettled(diff: Int)

        /**
         * Call after settle
         * @param success is complete
         */
        fun onPostSettled(success: Boolean)

        fun onCaptured()

        fun onReleased()
    }

    /**
     * Changing alpha and elevation of view
     */
    class AlphaElevationSideEffect : SideEffect {

        private var elevation: Float = 0f

        override fun onViewCaptured(child: View) {
            elevation = child.elevation
        }

        override fun apply(child: View, factor: Float) {
            child.elevation = elevation * (1f - abs(factor)) // special for elevation-aware view
            child.alpha = 1f - abs(factor)
        }
    }

    /**
     *  Restricts movement down a part of the view height
     *  @param maxFraction maximum position limit factor
     *  @param minFraction upward progress factor
     */
    class BelowFractionalClamp(
        private val maxFraction: Float = 1f,
        private val minFraction: Float = 1f
    ) : VerticalClamp {

        init {
            require(maxFraction > 0)
            require(minFraction > 0)
        }

        private var originTop: Int = -1

        override fun onViewCaptured(top: Int) {
            originTop = top
        }

        override fun constraint(height: Int, top: Int, dy: Int): Int {
            return min(top.toFloat(), originTop + height * maxFraction).toInt()
        }

        override fun downCast(distance: Int, top: Int, height: Int, dy: Int): Float {
            return distance / (height * maxFraction)
        }

        override fun upCast(distance: Int, top: Int, height: Int, dy: Int): Float {
            return distance / (height * minFraction)
        }
    }

    /**
     * Restricts movement up and down by part of the view height
     * @param maxFraction maximum position limit factor
     * @param minFraction minimum position limit factor
     */
    class FractionClamp(
        private val maxFraction: Float = 1f,
        private val minFraction: Float = 1f
    ) : VerticalClamp {

        init {
            require(maxFraction > 0)
            require(minFraction > 0)
        }

        private var originTop: Int = -1

        override fun onViewCaptured(top: Int) {
            originTop = top
        }

        override fun constraint(height: Int, top: Int, dy: Int): Int {
            val min = min(top, originTop + (height * minFraction).toInt())
            return max(min, originTop - (height * maxFraction).toInt())
        }

        override fun downCast(distance: Int, top: Int, height: Int, dy: Int): Float {
            return distance / (height * maxFraction)
        }

        override fun upCast(distance: Int, top: Int, height: Int, dy: Int): Float {
            return distance / (height * minFraction)
        }
    }

    /**
     * Applies the [delegate] only if view moves upwards
     */
    @Suppress("unused")
    class NegativeFactorFilterSideEffect(private val delegate: SideEffect) :
        SideEffect by delegate {

        override fun apply(child: View, factor: Float) {
            if (factor < 0) {
                delegate.apply(child, abs(factor))
            }
        }
    }

    /**
     * When the gesture is complete, it moves the view to the starting position
     */
    class OriginSettleAction : PostAction {

        private var originTop: Int = -1

        override fun onViewCaptured(child: View) {
            originTop = child.top
        }

        override fun releasedBelow(helper: ViewDragHelper, diff: Int, child: View): Boolean {
            return helper.settleCapturedViewAt(child.left, originTop)
        }

        override fun releasedAbove(helper: ViewDragHelper, diff: Int, child: View): Boolean {
            return helper.settleCapturedViewAt(child.left, originTop)
        }
    }

    /**
     * Responsible for changing the view position after the gesture is completed
     */
    interface PostAction {

        fun onViewCaptured(child: View)

        /**
         * View was released below initial position
         * @param helper motion animation "visitor"
         * @param diff released distance
         * @param child target view
         * @return whether or not the motion settle was triggered
         */
        fun releasedBelow(helper: ViewDragHelper, diff: Int, child: View): Boolean

        /**
         * View was released above initial position
         * @param helper motion animation "visitor"
         * @param diff released distance
         * @param child target view
         * @return whether or not the motion settle was triggered
         */
        fun releasedAbove(helper: ViewDragHelper, diff: Int, child: View): Boolean
    }

    /**
     * Common way for changing several properties of view at the same time
     */
    class PropertySideEffect(vararg props: Property<View, Float>) : SideEffect {

        private val properties = props
        private val capturedValues: Array<Float> = Array(props.size) { 0f }

        override fun onViewCaptured(child: View) {
            for ((index, property) in properties.withIndex()) {
                val value = property.get(child)
                capturedValues[index] = value
            }
        }

        override fun apply(child: View, factor: Float) {
            for ((index, property) in properties.withIndex()) {
                val value = capturedValues[index] * (1 - abs(factor))
                property.set(child, value)
            }
        }
    }

    /**
     * Reduces each move by several times and delegates the definition of the restriction
     * @param upSensitivity Sensitivity when moving up
     * @param delegate delegate
     * @param downSensitivity Sensitivity when moving down
     */

    class SensitivityClamp(
        private val upSensitivity: Float = 1f,
        private val delegate: VerticalClamp,
        private val downSensitivity: Float = 1f
    ) : VerticalClamp by delegate {

        override fun constraint(height: Int, top: Int, dy: Int): Int {
            val coefficient = if (dy > 0) downSensitivity else upSensitivity
            val newDy = (dy * coefficient).toInt()
            val newTop = top - dy + newDy
            return delegate.constraint(height, newTop, newDy)
        }
    }

    /**
     * When view moved downwards, it returns to the initial position.
     * Moves above - takes away from the screen.
     */
    class SettleOnTopAction : PostAction {

        private var originTop: Int = -1

        override fun onViewCaptured(child: View) {
            originTop = child.top
        }

        override fun releasedBelow(helper: ViewDragHelper, diff: Int, child: View): Boolean {
            return helper.settleCapturedViewAt(child.left, originTop)
        }

        override fun releasedAbove(helper: ViewDragHelper, diff: Int, child: View): Boolean {
            return helper.settleCapturedViewAt(
                child.left,
                if (diff < 0) -child.height else child.height
            )
        }
    }

    /**
     * Change of view properties depending on the progress of movement.
     * @see VerticalClamp
     */
    interface SideEffect {

        fun onViewCaptured(child: View)

        /**
         * Apply new property value for [child] depends on [factor]
         * @param child target movement
         * @param factor movement progress, from 0 to 1
         * @see [VerticalClamp.downCast]
         * @see [VerticalClamp.upCast]
         */
        fun apply(child: View, factor: Float)
    }

    /**
     * Sets limits on moving the view vertically
     */
    interface VerticalClamp {

        fun onViewCaptured(top: Int)

        /**
         * Limits maximum and/or minimum position for view
         * @param height height of view
         * @param top position of view
         * @param dy last movement of view
         * @return new position for view, see [android.view.View.getTop]
         */
        fun constraint(height: Int, top: Int, dy: Int): Int

        /**
         * Calculate movement progress down
         * @param distance total distance
         * @param top position of view
         * @param height height of view
         * @param dy last movement of view
         * @return movement progress down from 0 to 1
         * @see [SideEffect.apply]
         */
        fun downCast(distance: Int, top: Int, height: Int, dy: Int): Float

        /**
         * Calculate movement progress up
         * @param distance total distance
         * @param top position of view
         * @param height height of view
         * @param dy last movement of view
         * @return movement progress up from 0 to 1
         * @see [SideEffect.apply]
         */
        fun upCast(distance: Int, top: Int, height: Int, dy: Int): Float
    }

    /**
     * Does not change any properties of view
     */
    @Suppress("unused")
    class WithoutSideEffect : SideEffect {

        override fun onViewCaptured(child: View) {
            // ignore
        }

        override fun apply(child: View, factor: Float) {
            // ignore
        }
    }
}
