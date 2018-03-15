package com.example.rhythmprogressview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable

import java.util.ArrayList
import java.util.HashMap

/**
 * @author Mukhammadakbar
 */

abstract class Indicator(context: Context) : Drawable(), Animatable {

    private val mUpdateListeners = HashMap<ValueAnimator, ValueAnimator.AnimatorUpdateListener>()

    private var mAnimators: ArrayList<ValueAnimator>? = null
    private var alpha = 255
    private var drawBound = ZERO_BOUNDS_RECT
    private var viewContext = context

    private var mHasAnimators: Boolean = false

    private val mPaint = Paint()

    var color: Int
        get() = mPaint.color
        set(color) {
            mPaint.color = color
        }

    private val isStarted: Boolean
        get() {
            for (animator in mAnimators!!) {
                return animator.isStarted
            }
            return false
        }

    val width: Int
        get() = drawBound.width()

    val height: Int
        get() = drawBound.height()

    init {
        mPaint.color = Color.WHITE
        mPaint.style = Paint.Style.FILL
        mPaint.isAntiAlias = true
    }

    override fun setAlpha(alpha: Int) {
        this.alpha = alpha
    }

    override fun getAlpha(): Int {
        return alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun draw(canvas: Canvas) {
        drawLines(canvas, context = viewContext)
    }

    abstract fun drawLines(canvas: Canvas, context: Context)

    abstract fun onCreateAnimators(): ArrayList<ValueAnimator>

    override fun start() {
        ensureAnimators()

        if (mAnimators == null) {
            return
        }

        // If the animators has not ended, do nothing.
        if (isStarted) {
            return
        }
        startAnimators()
        invalidateSelf()
    }

    private fun startAnimators() {
        for (i in mAnimators!!.indices) {
            val animator = mAnimators!![i]

            //when the animator restart , add the updateListener again because they
            // was removed by animator stop .
            val updateListener = mUpdateListeners[animator]
            if (updateListener != null) {
                animator.addUpdateListener(updateListener)
            }

            animator.start()
        }
    }

    private fun stopAnimators() {
        if (mAnimators != null) {
            for (animator in mAnimators!!) {
                if (animator != null && animator.isStarted) {
                    animator.removeAllUpdateListeners()
                    animator.end()
                }
            }
        }
    }

    private fun ensureAnimators() {
        if (!mHasAnimators) {
            mAnimators = onCreateAnimators()
            mHasAnimators = true
        }
    }

    override fun stop() {
        stopAnimators()
    }

    override fun isRunning(): Boolean {
        return mAnimators!!
                .firstOrNull()
                ?.isRunning
                ?: false
    }

    /**
     * Your should use this to add AnimatorUpdateListener when
     * create animator , otherwise , animator doesn't work when
     * the animation restart .
     * @param updateListener
     */
    fun addUpdateListener(animator: ValueAnimator, updateListener: ValueAnimator.AnimatorUpdateListener) {
        mUpdateListeners.put(animator, updateListener)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        setDrawBounds(bounds)
    }

    fun setDrawBounds(drawBounds: Rect) {
        setDrawBounds(drawBounds.left, drawBounds.top, drawBounds.right, drawBounds.bottom)
    }

    fun setDrawBounds(left: Int, top: Int, right: Int, bottom: Int) {
        this.drawBound = Rect(left, top, right, bottom)
    }

    fun postInvalidate() {
        invalidateSelf()
    }
//
//    internal var scaleYFloats = floatArrayOf(SCALE_INIT,SCALE_INIT,SCALE_INIT,SCALE_INIT,SCALE_INIT,SCALE_INIT,SCALE_INIT)
//    internal var heights = floatArrayOf(0.1f, 0.2f, 0.3f, 0.3f, 0.2f, 0.2f, 0.09f)
//    internal var plusExtraHeights = floatArrayOf(0.5f, 0.21f, 0.17f, -0.26f, 0.6f, 0.08f, 0.67f)
//    internal var minusExtraHeights = floatArrayOf(0f, 0f, 0f, 0.35f, -0.4f, 0f, 0f)
//
//    internal var colors = intArrayOf(R.color.initialColor, R.color.lastColor, R.color.initialColor,R.color.initialColor,
//            R.color.lastColor, R.color.initialColor, R.color.initialColor)
//    internal var paints = arrayListOf(Paint())
//    init {
//        for (i in 0..6) {
//            val paint = Paint()
//            paint.color = ContextCompat.getColor(context, colors[i])
//            paints.add(paint)
//        }
//    }


//    fun drawLines(canvas: Canvas, context: Context) {
//        val translateX = (width / 14).toFloat()
//        val translateY = (height / 2).toFloat()
//        for (i in 0..6) {
//            val top = - heights[i] * height * (1 + minusExtraHeights[i])
//            val bottom = heights[i] * height * (1 + plusExtraHeights[i])
//            canvas.save()
//            canvas.translate((2 + i * 2f) * translateX - translateX / 2, translateY/2 + (top + bottom)/2 + height/4f)
//            canvas.scale(SCALE, scaleYFloats[i])
//            Log.d("top", "$top")
//            Log.d("bottom", "$bottom")
//            Log.d("trans", ((translateY + (-top + bottom)/2)/2).toString())
//            Log.d("height", (height/2).toString())
//            val rectF = RectF(-translateX / 1.5f,  - heights[i] * height, translateX / 1.5f, heights[i] * height)
//            canvas.drawRoundRect(rectF, 200f, 200f, paints[i+1])
//            canvas.restore()
//        }
//    }

//    fun onCreateAnimators(): ArrayList<ValueAnimator> {
//        val animators = ArrayList<ValueAnimator>()
//        val delays = longArrayOf(0, 100, 200, 300, 400, 500, 600)
//        for (i in 0..6) {
//            val scaleAnim = ValueAnimator.ofFloat(1f, 0.5f, 1f)
//            scaleAnim.duration = 600
//            scaleAnim.repeatCount = -1
//            scaleAnim.startDelay = delays[i]
//            scaleAnim.interpolator = DecelerateInterpolator()
//            addUpdateListener(scaleAnim, ValueAnimator.AnimatorUpdateListener {
//
//                scaleYFloats[i] = it.animatedValue as Float
//                postInvalidate()
//            })
//            animators.add(scaleAnim)
//        }
//        return animators
//    }


    companion object {
//        val SCALE = 1.0f
//        val SCALE_INIT = 0.5f
        val ZERO_BOUNDS_RECT = Rect()
    }
}