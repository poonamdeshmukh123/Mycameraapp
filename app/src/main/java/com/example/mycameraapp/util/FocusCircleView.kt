package com.example.mycameraapp.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

import android.util.AttributeSet
import android.view.View
import android.os.Handler
import android.os.Looper


class FocusCircleView(context:Context,attriuteset:AttributeSet): View(context,attriuteset)
{
    private val paint=Paint()
    var focalcircle: RectF?=null

    private var removeFocusRunnable = Runnable { }

    init {
        paint.color= Color.WHITE
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=5f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        focalcircle?.let {rect->
            val outer_radius=rect.width()/1.2f
            val inner_radius=outer_radius/2
            canvas.drawCircle(rect.centerX(),rect.centerY(),outer_radius,paint)
            canvas.drawCircle(rect.centerX(),rect.centerY(),inner_radius,paint)
            scheduleFocusCircleRemoval()
        }
    }

    private fun scheduleFocusCircleRemoval() {
        var handler = Handler(Looper.getMainLooper())

        handler.removeCallbacks(removeFocusRunnable)
        removeFocusRunnable= Runnable {
            focalcircle=null
            invalidate()
        }
        handler.postDelayed(removeFocusRunnable,2000)
    }

}