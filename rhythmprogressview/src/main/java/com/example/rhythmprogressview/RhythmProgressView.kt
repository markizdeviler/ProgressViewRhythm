package com.example.rhythmprogressview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.example.rhythmprogressview.extensions.isVisible
import com.example.rhythmprogressview.extensions.visible

/**
 * @author markizdeviler
 */

class RhythmProgressView : View {

    private var mStartTime: Long = -1
    private var mPostedHide = false
    private var mPostedShow = false
    private var mDismissed = false

    private val mDelayedHide = Runnable {
        mPostedHide = false
        mStartTime = -1
        visible(false)
    }

    private val mDelayedShow = Runnable {
        mPostedShow = false
        if (!mDismissed) {
            mStartTime = System.currentTimeMillis()
            visible(true)
        }
    }

    private var mRhythmProgress: RhythmProgressDrawable? = null
    private var mRhythmColor: Int = 0
    private var mShouldStartAnimationDrawable: Boolean = false

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        DEFAULT_INDICATOR = RhythmProgressDrawable()

        val a = context.obtainStyledAttributes(attrs, R.styleable.RhythmProgressView)
        setIndicator(DEFAULT_INDICATOR)
        mRhythmProgress?.setStyle(
            a.getColor(R.styleable.RhythmProgressView_animationLightColor, ContextCompat.getColor(context, R.color.lastColor)),
            a.getColor(R.styleable.RhythmProgressView_animationDarkColor, ContextCompat.getColor(context, R.color.dark)))
        a.recycle()
    }

    fun setStyle(colorLight: Int, colorDark: Int) {
        val drawable = RhythmProgressDrawable()
        DEFAULT_INDICATOR = drawable
        setIndicator(drawable)
        drawable.setStyle(colorLight, colorDark)
        postInvalidate()
    }

    private fun setIndicator(d: RhythmProgressDrawable?) {
        if (mRhythmProgress != d) {
            mRhythmProgress?.let {
                it.callback = null
                unscheduleDrawable(it)
            }

            mRhythmProgress = d
            setIndicatorColor(mRhythmColor)
            d?.callback = this
            postInvalidate()
        }
    }


    fun setIndicatorColor(color: Int) {
        mRhythmColor = color
        mRhythmProgress?.color = color
    }

    fun smoothToShow() {
        startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in))
        visible(true)
    }

    fun smoothToHide() {
        startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_out))
        visible(false)
    }

    fun hide() {
        mDismissed = true
        removeCallbacks(mDelayedShow)
        val diff = System.currentTimeMillis() - mStartTime
        if (diff >= MIN_SHOW_TIME || mStartTime.toInt() == -1) {
            visible(false)
        } else {
            if (!mPostedHide) {
                postDelayed(mDelayedHide, MIN_SHOW_TIME - diff)
                mPostedHide = true
            }
        }
    }

    fun show() {
        mStartTime = -1
        mDismissed = false
        removeCallbacks(mDelayedHide)
        if (!mPostedShow) {
            postDelayed(mDelayedShow, MIN_DELAY.toLong())
            mPostedShow = true
        }
    }

    override fun verifyDrawable(who: Drawable): Boolean =
        who === mRhythmProgress || super.verifyDrawable(who)

    private fun startAnimation() {
        if (isVisible()) {
            if (mRhythmProgress is Animatable)
                mShouldStartAnimationDrawable = true
            postInvalidate()
        }
    }

    private fun stopAnimation() {
        if (mRhythmProgress is Animatable) {
            mRhythmProgress?.stop()
            mShouldStartAnimationDrawable = false
        }
        postInvalidate()
    }

    override fun setVisibility(v: Int) {
        if (visibility != v) {
            super.setVisibility(v)
            if (v == GONE)
                stopAnimation()
            else startAnimation()
        }
    }

    override fun invalidateDrawable(dr: Drawable) {
        if (verifyDrawable(dr)) {
            invalidate()
        } else super.invalidateDrawable(dr)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) = updateDrawableBounds(w, h)

    private fun updateDrawableBounds(w: Int, h: Int) {
        val w = w - (paddingRight + paddingLeft)
        val h = h - (paddingTop + paddingBottom)

        var right = w
        var bottom = h
        var top = 0
        var left = 0

        mRhythmProgress?.let {
            val intrinsicWidth = it.intrinsicWidth
            val intrinsicHeight = it.intrinsicHeight
            val intrinsicAspect = intrinsicWidth.toFloat() / intrinsicHeight
            val boundAspect = w.toFloat() / h
            if (intrinsicAspect != boundAspect) {
                if (boundAspect > intrinsicAspect) {
                    val width = (h * intrinsicAspect).toInt()
                    left = (w - width) / 2
                    right = left + width
                } else {
                    val height = (w * (1 / intrinsicAspect)).toInt()
                    top = (h - height) / 2
                    bottom = top + height
                }
            }
            it.setBounds(left, top, right, bottom)
        }
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawTrack(canvas)
    }

    private fun drawTrack(canvas: Canvas) {
        mRhythmProgress?.let {
            val saveCount = canvas.save()
            canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())

            it.draw(canvas)
            canvas.restoreToCount(saveCount)

            if (mShouldStartAnimationDrawable) {
                (it as Animatable).start()
                mShouldStartAnimationDrawable = false
            }
        }
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        updateDrawableState()

        val dw = paddingLeft + paddingRight
        val dh = paddingTop + paddingBottom

        val measuredWidth = resolveSizeAndState(dw, widthMeasureSpec, 0)
        val measuredHeight = resolveSizeAndState(dh, heightMeasureSpec, 0)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        updateDrawableState()
    }

    private fun updateDrawableState() {
        if (mRhythmProgress?.isStateful == true)
            mRhythmProgress?.state = drawableState
    }

    override fun drawableHotspotChanged(x: Float, y: Float) {
        super.drawableHotspotChanged(x, y)
        mRhythmProgress?.setHotspot(x, y)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
        removeCallbacks()
    }

    override fun onDetachedFromWindow() {
        stopAnimation()
        super.onDetachedFromWindow()
        removeCallbacks()
    }

    private fun removeCallbacks() {
        removeCallbacks(mDelayedHide)
        removeCallbacks(mDelayedShow)
    }

    companion object {
        private var DEFAULT_INDICATOR: RhythmProgressDrawable? = null
        private const val MIN_SHOW_TIME = 600 // ms
        private const val MIN_DELAY = 600 // ms
    }
}