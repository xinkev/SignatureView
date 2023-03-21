package com.xinkev.sign

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Paint
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
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xinkev.sign.ui.theme.SignatureTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignatureTheme {
                val state = rememberSignaturePadState()
                val context = LocalContext.current

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
                        Button(onClick = {
                            val file = File(context.cacheDir, "hi.jpg")
                            state.capture()
                                .compress(Bitmap.CompressFormat.JPEG, 85, file.outputStream())
                        }) {
                            Text(text = "Save")
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
    var currentPosition by remember { mutableStateOf(Offset.Unspecified) }

    Canvas(
        modifier = modifier.then(
            Modifier.pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        state.path.moveTo(it.x, it.y)
                        currentPosition = Offset(it.x, it.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        state.path.quadraticBezierTo(
                            currentPosition.x,
                            currentPosition.y,
                            (currentPosition.x + it.x) / 2,
                            (currentPosition.y + it.y) / 2

                        )

                        currentPosition = Offset(it.x, it.y)
                        state.invalidate()
                    }

                    MotionEvent.ACTION_UP -> {
                        state.path.lineTo(it.x, it.y)
                        currentPosition = Offset.Unspecified
                    }
                }
                true
            }
        )
    ) {
        if (state.draws > 0) {
            drawPath(
                path = state.path,
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

@Stable
class SignaturePadState {
    var draws by mutableStateOf(0)
        private set

    val path = Path()

    fun reset() {
        draws = 0
        path.reset()
    }

    internal fun invalidate() {
        draws++
    }

    fun capture(): Bitmap {
        val bitmap = Bitmap.createBitmap(1000, 1000, ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = Paint(Paint.DITHER_FLAG).apply {
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            isDither = true
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawColor(Color.White.toArgb())
        canvas.drawPath(path.asAndroidPath(), paint)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return bitmap
    }
}

@Composable
fun rememberSignaturePadState() = remember {
    SignaturePadState()
}
