package com.xinkev.sign

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
                    SignaturePad(
                        brushSize = 1.dp,
                        brushColor = Color.Black,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    brushSize: Dp,
    brushColor: Color,
) {
    val path = remember { Path() }
    var draw by remember { mutableStateOf(0) }
    var currentPosition by remember { mutableStateOf(Offset.Unspecified) }

    Canvas(
        modifier = modifier.then(
            Modifier.pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        path.moveTo(it.x, it.y)
                        currentPosition = Offset(it.x, it.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        path.quadraticBezierTo(
                            currentPosition.x,
                            currentPosition.y,
                            (currentPosition.x + it.x) / 2,
                            (currentPosition.y + it.y) / 2

                        )

                        currentPosition = Offset(it.x, it.y)
                        draw++
                    }

                    MotionEvent.ACTION_UP -> {
                        path.lineTo(it.x, it.y)
                        currentPosition = Offset.Unspecified
                    }
                }
                true
            }
        )
    ) {
        if (draw > 0) {
            drawPath(
                path = path,
                color = brushColor,
                style = Stroke(
                    width = brushSize.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
