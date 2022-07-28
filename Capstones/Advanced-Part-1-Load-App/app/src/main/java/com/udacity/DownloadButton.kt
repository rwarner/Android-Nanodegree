package com.udacity

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat.getColor
import kotlin.properties.Delegates

class DownloadButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    var progress = 0.0f

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->

        when(new) {
            ButtonState.Click -> {
                invalidate()
            }
            ButtonState.Downloading -> {
                invalidate()
            }
            ButtonState.Completed -> {
                invalidate()
            }

        }
    }

    /**
     * Setup our Paint views below
     *
     * Each paint view reflects a "drawing" that will be executed in the below methods
     * especially onDraw()
     */
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.BOLD)
        color = Color.WHITE
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        color = getColor(context, R.color.colorPrimary)
    }

    private val downloadingPaintBox = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        color = getColor(context, R.color.colorPrimaryDark)
    }

//    private val downloadingPaintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//        style = Paint.Style.FILL
//        textAlign = Paint.Align.CENTER
//        color = getColor(context, R.color.colorAccent)
//    }

    init {
        isClickable = true
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawRect(0.0f, heightSize.toFloat(), widthSize.toFloat(), 0.0f, backgroundPaint)

        when(buttonState) {
            ButtonState.Downloading -> {
                // Increment width of rectangle to emulator progress
                val widthProgress = (widthSize.toFloat() * progress)
                canvas?.drawRect(0.0f, heightSize.toFloat(), widthProgress, 0.0f, downloadingPaintBox)

//                val radius =  (Integer.min(width, height) / 2.0 * 0.8).toFloat()
//                canvas?.drawCircle( (width / 2).toFloat() + 225.0f, (height / 2).toFloat(), 50.0f, downloadingPaintCircle)

            }
            else -> {
                // Get rid of progress bar, if we're not downloading
                canvas?.drawRect(0.0f, 0.0f, 0.0f, 0.0f, downloadingPaintBox)
                progress = 0.0f
            }
        }

        // Update the label depending on when the button changes
        val label = when(buttonState) {
            ButtonState.Click -> "Download"
            ButtonState.Downloading -> "Downloading"
            ButtonState.Completed -> "Completed"
        }

        // Draw our text on top of everything
        canvas?.drawText(label, (width / 2).toFloat(), ((height / 4) * 2.5).toFloat(), textPaint)

    }

    /**
     * Setup coordinates / size of our layout to work with
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    /**
     * Allow outside classes to update loadingState
     */
    fun setLoadingState(buttonState: ButtonState) {
        this.buttonState = buttonState
    }

    /**
     * Allow outside classes to obtain loadingState
     */
    fun getLoadingState(): ButtonState {
        return this.buttonState
    }

}