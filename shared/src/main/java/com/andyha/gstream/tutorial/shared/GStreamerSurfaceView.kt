package com.andyha.gstream.tutorial.shared

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceView

// A simple SurfaceView whose width and height can be set from the outside
class GStreamerSurfaceView : SurfaceView {
    var mediaWidth = 320
    var mediaHeight = 240

    // Mandatory constructors, they do not do much
    constructor(
        context: Context?, attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?) : super(context) {}

    // Called by the layout manager to find out our size and give us some rules.
    // We will try to maximize our size, and preserve the media's aspect ratio if
    // we are given the freedom to do so.
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = 0
        var height = 0
        val wmode = MeasureSpec.getMode(widthMeasureSpec)
        val hmode = MeasureSpec.getMode(heightMeasureSpec)
        val wsize = MeasureSpec.getSize(widthMeasureSpec)
        val hsize = MeasureSpec.getSize(heightMeasureSpec)
        Log.i("GStreamer", "onMeasure called with " + mediaWidth + "x" + mediaHeight)
        when (wmode) {
            MeasureSpec.AT_MOST -> {
                width = if (hmode == MeasureSpec.EXACTLY) {
                    (hsize * mediaWidth / mediaHeight).coerceAtMost(wsize)
                } else {
                    wsize
                }
            }
            MeasureSpec.EXACTLY -> width = wsize
            MeasureSpec.UNSPECIFIED -> width = mediaWidth
        }
        when (hmode) {
            MeasureSpec.AT_MOST -> {
                height = if (wmode == MeasureSpec.EXACTLY) {
                    (wsize * mediaHeight / mediaWidth).coerceAtMost(hsize)
                } else {
                    hsize
                }
            }
            MeasureSpec.EXACTLY -> height = hsize
            MeasureSpec.UNSPECIFIED -> height = mediaHeight
        }

        // Finally, calculate best size when both axis are free
        if (hmode == MeasureSpec.AT_MOST && wmode == MeasureSpec.AT_MOST) {
            val correctHeight = width * mediaHeight / mediaWidth
            val correctWidth = height * mediaWidth / mediaHeight
            if (correctHeight < height) height = correctHeight else width = correctWidth
        }

        // Obey minimum size
        width = suggestedMinimumWidth.coerceAtLeast(width)
        height = suggestedMinimumHeight.coerceAtLeast(height)
        setMeasuredDimension(width, height)
    }
}