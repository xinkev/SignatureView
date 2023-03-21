package com.xinkev.sign

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xinkev.sign.ui.theme.SignatureTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignatureTheme {
                val state = rememberSignaturePadState()

                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    SignaturePad(
                        brushSize = 1.dp,
                        brushColor = Color.Black,
                        modifier = Modifier.fillMaxSize(0.9f),
                        state = state
                    )
                    Row {
                        Button(onClick = state::reset, enabled = state.draws > 0) {
                            Text(text = "Reset")
                        }

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignaturePad(
    state: SignaturePadState,
    modifier: Modifier = Modifier,
    brushSize: Dp,
    brushColor: Color
) {
    val path = remember { Path() }
    var currentPosition by remember { mutableStateOf(Offset.Unspecified) }

    if (state.draws == 0) path.reset()

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
                        state.invalidate()
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

@Stable
class SignaturePadState {
    var draws by mutableStateOf(0)
        private set

    fun reset() {
        draws = 0
    }

    internal fun invalidate() {
        draws++
    }
}

@Composable
fun rememberSignaturePadState() = remember {
    SignaturePadState()
}
