package com.xinkev.sign.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class SignatureView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    private val paint: Paint = Paint()
    private val path: Path = Path()

    /**
     * Optimizes painting by invalidating the smallest possible area.
     */
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val dirtyRect = RectF()

    init {
        paint.isAntiAlias = true
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = STROKE_WIDTH
    }

    /**
     * Erases the signature.
     */
    fun clear() {
        path.reset()

        // Repaints the entire view.
        invalidate()
    }

    protected override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventX = event.x
        val eventY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(eventX, eventY)
                lastTouchX = eventX
                lastTouchY = eventY
                // There is no end point yet, so don't waste cycles invalidating.
                return true
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                // Start tracking the dirty region.
                resetDirtyRect(eventX, eventY)

                // When the hardware tracks events faster than they are delivered, the
                // event will contain a history of those skipped points.
                val historySize = event.historySize
                var i = 0
                while (i < historySize) {
                    val historicalX = event.getHistoricalX(i)
                    val historicalY = event.getHistoricalY(i)
                    expandDirtyRect(historicalX, historicalY)
                    path.lineTo(historicalX, historicalY)
                    i++
                }

                // After replaying history, connect the line to the touch point.
                path.lineTo(eventX, eventY)
            }
            else -> {
                return false
            }
        }

        // Include half the stroke width to avoid clipping.
        invalidate(
            (dirtyRect.left - HALF_STROKE_WIDTH).toInt(),
            (dirtyRect.top - HALF_STROKE_WIDTH).toInt(),
            (dirtyRect.right + HALF_STROKE_WIDTH).toInt(),
            (dirtyRect.bottom + HALF_STROKE_WIDTH).toInt()
        )
        lastTouchX = eventX
        lastTouchY = eventY
        return true
    }

    /**
     * Called when replaying history to ensure the dirty region includes all
     * points.
     */
    private fun expandDirtyRect(historicalX: Float, historicalY: Float) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX
        }
        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY
        }
    }

    /**
     * Resets the dirty region when the motion event occurs.
     */
    private fun resetDirtyRect(eventX: Float, eventY: Float) {

        // The lastTouchX and lastTouchY were set when the ACTION_DOWN
        // motion event occurred.
        dirtyRect.left = lastTouchX.coerceAtMost(eventX)
        dirtyRect.right = lastTouchX.coerceAtLeast(eventX)
        dirtyRect.top = lastTouchY.coerceAtMost(eventY)
        dirtyRect.bottom = lastTouchY.coerceAtLeast(eventY)
    }

    companion object {
        private const val STROKE_WIDTH = 5f

        /** Need to track this so the dirty region can accommodate the stroke.  */
        private const val HALF_STROKE_WIDTH = STROKE_WIDTH / 2
    }
}