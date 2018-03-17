package com.example.rhythmprogressview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import java.util.ArrayList
import java.util.HashMap

/**
 * @author Mukhammadakbar
 */

class RhythmProgressDrawable : Drawable(), Animatable {

    private var scaleYFloats = floatArrayOf(SCALE_INIT,SCALE_INIT,SCALE_INIT,SCALE_INIT,SCALE_INIT,SCALE_INIT,SCALE_INIT)
    private var heights = floatArrayOf(0.1f, 0.2f, 0.3f, 0.3f, 0.2f, 0.2f, 0.09f)
    private var plusExtraHeights = floatArrayOf(0.5f, 0.21f, 0.17f, -0.26f, 0.6f, 0.08f, 0.67f)
    private var minusExtraHeights = floatArrayOf(0f, 0f, 0f, 0.35f, -0.4f, 0f, 0f)
    private var paints = arrayListOf<Paint>()
    private val mUpdateListeners = HashMap<ValueAnimator, ValueAnimator.AnimatorUpdateListener>()
    private var mAnimators: ArrayList<ValueAnimator>? = null
    private var alpha = 255
    private var drawBound = ZERO_BOUNDS_RECT
    private var mHasAnimators: Boolean = false

    private val mPaint = Paint()

    var color: Int
        get() = mPaint.color
        set(color) {
            mPaint.color = color
        }

    private val isStarted: Boolean get() = mAnimators!!.firstOrNull()?.isStarted ?: false

    private val width: Int get() = drawBound.width()

    private val height: Int get() = drawBound.height()

    init {
        mPaint.color = Color.WHITE
        mPaint.style = Paint.Style.FILL
        mPaint.isAntiAlias = true
    }

    fun setStyle( colorLight: Int, colorDark: Int, context: Context) {
        Log.d("style", "$colorDark | $colorLight")
        for (i in 0..6) {
            val paint = Paint()
            when (i) {
                0, 2, 3, 5, 6 -> paint.color = ContextCompat.getColor(context, colorLight)
                1, 4 -> paint.color = ContextCompat.getColor(context, colorDark)
            }
            paints.add(paint)
        }
    }

    override fun setAlpha(alpha: Int) {
        this.alpha = alpha
    }

    override fun getAlpha(): Int = alpha

    override fun getOpacity(): Int  = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun draw(canvas: Canvas) = drawLines(canvas)

    override fun start() {
        ensureAnimators()

        if (mAnimators == null) return

        if (isStarted) return

        startAnimators()
        invalidateSelf()
    }

    private fun startAnimators() {
        for (i in mAnimators!!.indices) {
            val animator = mAnimators!![i]
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

    override fun stop() = stopAnimators()

    override fun isRunning(): Boolean = mAnimators!!.firstOrNull()?.isRunning ?: false

    private fun addUpdateListener(animator: ValueAnimator, updateListener: ValueAnimator.AnimatorUpdateListener) =
        mUpdateListeners.put(animator, updateListener)


    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        setDrawBounds(bounds)
    }

    private fun setDrawBounds(drawBounds: Rect) =
            setDrawBounds(drawBounds.left, drawBounds.top, drawBounds.right, drawBounds.bottom)


    private fun setDrawBounds(left: Int, top: Int, right: Int, bottom: Int) {
        this.drawBound = Rect(left, top, right, bottom)
    }

    private fun drawLines(canvas: Canvas) {
        val translateX = (width / 14).toFloat()
        val translateY = (height / 2).toFloat()
        for (i in 0..6) {
            canvas.save()
            canvas.translate((2 + i * 2f) * translateX - translateX / 2, translateY + (heights[i] * height * (plusExtraHeights[i] - minusExtraHeights[i]))/2)
            canvas.scale(SCALE, scaleYFloats[i])
            val rectF = RectF(-translateX / 1.5f,  - heights[i] * height, translateX / 1.5f, heights[i] * height)
            canvas.drawRoundRect(rectF, translateX/2, translateX/2, paints[i])
            canvas.restore()
        }
    }

    private fun onCreateAnimators(): ArrayList<ValueAnimator> {
        val animators = ArrayList<ValueAnimator>()
        val delays = longArrayOf(0, 100, 200, 300, 400, 500, 600)
        for (i in 0..6) {
            val scaleAnim = ValueAnimator.ofFloat(1f, 0.6f, 1f)
            scaleAnim.duration = 600
            scaleAnim.repeatCount = -1
            scaleAnim.startDelay = delays[i]
            scaleAnim.interpolator = DecelerateInterpolator()
            addUpdateListener(scaleAnim, ValueAnimator.AnimatorUpdateListener {
                scaleYFloats[i] = it.animatedValue as Float
                invalidateSelf()
            })
            animators.add(scaleAnim)
        }
        return animators
    }

    companion object {
        val SCALE = 1.0f
        val SCALE_INIT = 0.6f
        val ZERO_BOUNDS_RECT = Rect()
    }
}