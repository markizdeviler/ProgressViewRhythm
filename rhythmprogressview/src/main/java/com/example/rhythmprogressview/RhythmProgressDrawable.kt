package com.example.rhythmprogressview

import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import java.util.*

/**
 * @author markizdeviler
 */

class RhythmProgressDrawable : Drawable(), Animatable {

    private var scaleYFloats = floatArrayOf(SCALE_INIT, SCALE_INIT, SCALE_INIT, SCALE_INIT, SCALE_INIT, SCALE_INIT, SCALE_INIT)
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

    private val isStarted: Boolean get() = mAnimators?.firstOrNull()?.isStarted ?: false

    private val width: Int get() = drawBound.width()

    private val height: Int get() = drawBound.height()

    init {
        mPaint.color = Color.WHITE
        mPaint.style = Paint.Style.FILL
        mPaint.isAntiAlias = true
    }

    fun setStyle(@ColorInt colorLight: Int, @ColorInt colorDark: Int) {
        for (i in 0..6) {
            val paint = Paint()
            when (i) {
                0, 2, 3, 5, 6 -> paint.color = colorLight
                1, 4 -> paint.color = colorDark
            }
            paints.add(paint)
        }
    }

    override fun setAlpha(alpha: Int) {
        this.alpha = alpha
    }

    override fun getAlpha(): Int = alpha

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun draw(canvas: Canvas) = drawLines(canvas)

    override fun start() {
        ensureAnimators()
        if (mAnimators != null && !isStarted)
            startAnimators()
    }

    private fun startAnimators() {
        mAnimators?.forEach { animator ->
            mUpdateListeners[animator]?.let {
                animator.addUpdateListener(it)
            }
            animator.start()
        }
        invalidateSelf()
    }

    private fun stopAnimators() {
        mAnimators?.filter { it.isStarted }
            ?.forEach { animator ->
                animator.removeAllUpdateListeners()
                animator.end()
            }
    }

    private fun ensureAnimators() {
        if (!mHasAnimators) {
            mAnimators = onCreateAnimators()
            mHasAnimators = true
        }
    }

    override fun stop() = stopAnimators()

    override fun isRunning(): Boolean = mAnimators?.firstOrNull()?.isRunning ?: false

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        drawBound = Rect(bounds.left, bounds.top, bounds.right, bounds.bottom)
    }

    private fun drawLines(canvas: Canvas) {
        val translateX = (width / 15).toFloat()
        val translateY = (height / 2).toFloat()
        (0..6).forEach { index ->
            canvas.save()
            canvas.translate((2 + 2 * index) * translateX - translateX / 2, translateY + (heights[index] * height * (plusExtraHeights[index] - minusExtraHeights[index])) / 2)
            canvas.scale(SCALE, scaleYFloats[index])
            val rectF = RectF(-translateX / 1.3f, -heights[index] * height, translateX / 1.3f, heights[index] * height)
            canvas.drawRoundRect(rectF, translateX, translateX, paints[index])
            canvas.restore()
        }
    }

    private fun onCreateAnimators(): ArrayList<ValueAnimator> {
        val animators = ArrayList<ValueAnimator>()
        val delays = longArrayOf(0, 100, 200, 300, 400, 500, 600)
        (0..6).forEach { index ->
            ValueAnimator.ofFloat(1f, 0.6f, 1f).apply {
                duration = 600
                repeatCount = -1
                startDelay = delays[index]
                interpolator = DecelerateInterpolator()
            }?.also { scaleAnim ->
                mUpdateListeners[scaleAnim] = ValueAnimator.AnimatorUpdateListener {
                    scaleYFloats[index] = it.animatedValue as Float
                    invalidateSelf()
                }
                animators.add(scaleAnim)
            }
        }
        return animators
    }

    companion object {
        const val SCALE = 1.0f
        const val SCALE_INIT = 0.6f
        val ZERO_BOUNDS_RECT = Rect()
    }
}