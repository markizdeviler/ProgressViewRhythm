package com.example.rhythmprogressview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

import android.animation.ValueAnimator
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.animation.*

import java.util.ArrayList

/**
 * @author Mukhammadakbar
 */
class LineIndicator(context: Context) : Indicator(context = context) {

    internal var scaleYFloats = floatArrayOf(SCALE_INIT, SCALE_INIT, SCALE_INIT, SCALE_INIT, SCALE_INIT, SCALE_INIT, SCALE_INIT)
    internal var heights = floatArrayOf(0.1f, 0.2f, 0.3f, 0.3f, 0.2f, 0.2f, 0.09f)
    internal var plusExtraHeights = floatArrayOf(0.5f, 0.21f, 0.17f, -0.26f, 0.6f, 0.08f, 0.67f)
    internal var minusExtraHeights = floatArrayOf(0f, 0f, 0f, 0.35f, -0.4f, 0f, 0f)

    internal var colors = intArrayOf(R.color.initialColor, R.color.lastColor, R.color.initialColor,R.color.initialColor,
            R.color.lastColor, R.color.initialColor, R.color.initialColor)
    internal var paints = arrayListOf(Paint())
    init {
        for (i in 0..6) {
            val paint = Paint()
            paint.color = ContextCompat.getColor(context, colors[i])
            paints.add(paint)
        }
    }


    override fun drawLines(canvas: Canvas, context: Context) {
        val translateX = (width / 14).toFloat()
        val translateY = (height / 2).toFloat()
        for (i in 0..6) {
            val top = - heights[i] * height * (1 + minusExtraHeights[i])
            val bottom = heights[i] * height * (1 + plusExtraHeights[i])
            canvas.save()
            canvas.translate((2 + i * 2f) * translateX - translateX / 2, translateY/2 + (top + bottom)/2 + height/4f)
            canvas.scale(SCALE, scaleYFloats[i])
            Log.d("top", "$top")
            Log.d("bottom", "$bottom")
            Log.d("trans", ((translateY + (-top + bottom)/2)/2).toString())
            Log.d("height", (height/2).toString())
            val rectF = RectF(-translateX / 1.5f,  - heights[i] * height, translateX / 1.5f, heights[i] * height)
            canvas.drawRoundRect(rectF, 200f, 200f, paints[i+1])
            canvas.restore()
        }
    }

    override fun onCreateAnimators(): ArrayList<ValueAnimator> {
        val animators = ArrayList<ValueAnimator>()
        val delays = longArrayOf(0, 100, 200, 300, 400, 500, 600)
        for (i in 0..6) {
            val scaleAnim = ValueAnimator.ofFloat(1f, 0.5f, 1f)
            scaleAnim.duration = 600
            scaleAnim.repeatCount = -1
            scaleAnim.startDelay = delays[i]
            scaleAnim.interpolator = DecelerateInterpolator()
            addUpdateListener(scaleAnim, ValueAnimator.AnimatorUpdateListener {

                scaleYFloats[i] = it.animatedValue as Float
                postInvalidate()
            })
            animators.add(scaleAnim)
        }
        return animators
    }


    companion object {
        val SCALE = 1.0f
        val SCALE_INIT = 0.5f
    }
}
