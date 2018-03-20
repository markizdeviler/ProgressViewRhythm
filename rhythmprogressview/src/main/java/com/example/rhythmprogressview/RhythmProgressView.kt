package com.example.rhythmprogressview

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils

/**
 * @author Mukhammadakbar
 */

class RhythmProgressView : View {

    private var mStartTime: Long = -1
    private var mPostedHide = false
    private var mPostedShow = false
    private var mDismissed = false

    private val mDelayedHide = Runnable {
        mPostedHide = false
        mStartTime = -1
        visibility = View.GONE
    }

    private val mDelayedShow = Runnable {
        mPostedShow = false
        if (!mDismissed) {
            mStartTime = System.currentTimeMillis()
            visibility = View.VISIBLE
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

        val a = context.obtainStyledAttributes(attrs, R.styleable.RhythmProgressAttr)
        setIndicator(DEFAULT_INDICATOR)
        mRhythmProgress?.setStyle(
                a.getResourceId(R.styleable.RhythmProgressAttr_animationLightColor, R.color.lastColor),
                a.getResourceId(R.styleable.RhythmProgressAttr_animationDarkColor, R.color.dark), context)
        a.recycle()
    }

    fun setStyle(colorLoght: Int, colorDark: Int){
        val drawable = RhythmProgressDrawable()
        DEFAULT_INDICATOR = drawable
        setIndicator(drawable)
        drawable.setStyle(colorLoght, colorDark, context)
        postInvalidate()
    }

    private fun setIndicator(d: RhythmProgressDrawable?) {
        if (mRhythmProgress != d) {
            if (mRhythmProgress != null) {
                mRhythmProgress!!.callback = null
                unscheduleDrawable(mRhythmProgress)
            }

            mRhythmProgress = d
            setIndicatorColor(mRhythmColor)
            if (d != null) {
                d.callback = this
            }
            postInvalidate()
        }
    }


    fun setIndicatorColor(color: Int) {
        this.mRhythmColor = color
        mRhythmProgress!!.color = color
    }

    fun smoothToShow() {
        startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in))
        visibility = View.VISIBLE
    }

    fun smoothToHide() {
        startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_out))
        visibility = View.GONE
    }

    fun hide() {
        mDismissed = true
        removeCallbacks(mDelayedShow)
        val diff = System.currentTimeMillis() - mStartTime
        if (diff >= MIN_SHOW_TIME || mStartTime.toInt() == -1) {
            visibility = View.GONE
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

    override fun verifyDrawable(who: Drawable): Boolean {
        return who === mRhythmProgress || super.verifyDrawable(who)
    }

    internal fun startAnimation() {
        if (visibility != View.VISIBLE) return
        if (mRhythmProgress is Animatable) mShouldStartAnimationDrawable = true
        postInvalidate()
    }

    internal fun stopAnimation() {
        if (mRhythmProgress is Animatable) {
            mRhythmProgress!!.stop()
            mShouldStartAnimationDrawable = false
        }
        postInvalidate()
    }

    override fun setVisibility(v: Int) {
        if (visibility != v) {
            super.setVisibility(v)
            if (v == View.GONE || v == View.INVISIBLE) stopAnimation()
            else startAnimation()
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.GONE || visibility == View.INVISIBLE) stopAnimation()
        else startAnimation()
    }

    override fun invalidateDrawable(dr: Drawable) {
        if (verifyDrawable(dr)) {
            val dirty = dr.bounds
            val scrollX = scrollX + paddingLeft
            val scrollY = scrollY + paddingTop
            invalidate(dirty.left + scrollX, dirty.top + scrollY,
                    dirty.right + scrollX, dirty.bottom + scrollY)
        } else super.invalidateDrawable(dr)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) = updateDrawableBounds(w, h)

    private fun updateDrawableBounds(w: Int, h: Int) {
        var w = w
        var h = h
        w -= paddingRight + paddingLeft
        h -= paddingTop + paddingBottom

        var right = w
        var bottom = h
        var top = 0
        var left = 0

        if (mRhythmProgress != null) {
            val intrinsicWidth = mRhythmProgress!!.intrinsicWidth
            val intrinsicHeight = mRhythmProgress!!.intrinsicHeight
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
            mRhythmProgress!!.setBounds(left, top, right, bottom)
        }
    }

    @Synchronized override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawTrack(canvas)
    }

    private fun drawTrack(canvas: Canvas) {
        val d = mRhythmProgress
        if (d != null) {
            val saveCount = canvas.save()
            canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())

            d.draw(canvas)
            canvas.restoreToCount(saveCount)

            if (mShouldStartAnimationDrawable && d is Animatable) {
                (d as Animatable).start()
                mShouldStartAnimationDrawable = false
            }
        }
    }

    @Synchronized override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var dw = 0
        var dh = 0

        updateDrawableState()

        dw += paddingLeft + paddingRight
        dh += paddingTop + paddingBottom

        val measuredWidth = View.resolveSizeAndState(dw, widthMeasureSpec, 0)
        val measuredHeight = View.resolveSizeAndState(dh, heightMeasureSpec, 0)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        updateDrawableState()
    }

    private fun updateDrawableState() {
        val state = drawableState
        if (mRhythmProgress != null && mRhythmProgress!!.isStateful)
            mRhythmProgress!!.state = state
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun drawableHotspotChanged(x: Float, y: Float) {
        super.drawableHotspotChanged(x, y)
        if (mRhythmProgress != null) mRhythmProgress!!.setHotspot(x, y)
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
        private val MIN_SHOW_TIME = 600 // ms
        private val MIN_DELAY = 600 // ms
    }
}