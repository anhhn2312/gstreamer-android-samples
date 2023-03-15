package org.freedesktop.gstreamer.tutorials.tutorial_1

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import org.freedesktop.gstreamer.GStreamer

class Tutorial1 : Activity() {
    private external fun nativeGetGStreamerInfo(): String

    // Called when the activity is first created.
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            GStreamer.init(this)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        setContentView(R.layout.main)
        val tv = findViewById<View>(R.id.textview_info) as TextView
        tv.text = "Welcome to ${nativeGetGStreamerInfo()}!"
    }

    companion object {
        init {
            System.loadLibrary("gstreamer_android")
            System.loadLibrary("tutorial-1")
        }
    }
}