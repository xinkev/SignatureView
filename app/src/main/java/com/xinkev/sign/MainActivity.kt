package com.xinkev.sign

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.xinkev.sign.ui.theme.SignatureTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignatureTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SignaturePad()
                }
            }
        }
    }
}

@Composable
fun SignaturePad() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { SignaturePad(it) }
    )
}


class SignaturePad(
    context: Context,
    attributeSet: AttributeSet? = null
) : View(context, attributeSet) {
    private val path = Path()
    private var currentOffset = Offset.Unspecified
    private val brushPaint = Paint()
    private val brushSize: Float = 0.1f
    private val paintColor = Color.BLACK
    private val canvasPaint = Paint(Paint.DITHER_FLAG)
    private lateinit var bitmap: Bitmap
    private lateinit var canvas: Canvas

    init {
        brushPaint.apply {
            isAntiAlias = true
            color = paintColor
            isAntiAlias = true
            strokeWidth = brushSize
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
    }

    fun clear() {
        path.reset()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, 0f, 0f, canvasPaint)
        canvas.drawPath(path, brushPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventX = event.x
        val eventY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(eventX, eventY)
                currentOffset = Offset(eventX, eventY)
            }
            MotionEvent.ACTION_MOVE -> {
                path.quadTo(
                    currentOffset.x,
                    currentOffset.y,
                    (currentOffset.x + eventX) / 2,
                    (currentOffset.y + eventY) / 2

                )
//                path.lineTo(eventX, eventY)
                canvas.drawPath(path, brushPaint)
                currentOffset = Offset(eventX, eventY)

            }

            MotionEvent.ACTION_UP -> {
                path.lineTo(eventX, eventY)
                currentOffset = Offset.Unspecified
            }
            else -> return false
        }
        invalidate()
        return true
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SignatureTheme {
        SignaturePad()
    }
}

