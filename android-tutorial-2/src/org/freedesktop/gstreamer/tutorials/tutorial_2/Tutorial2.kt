package org.freedesktop.gstreamer.tutorials.tutorial_2

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import org.freedesktop.gstreamer.GStreamer.init

class Tutorial2 : Activity() {
    private external fun nativeInit() // Initialize native code, build pipeline, etc
    private external fun nativeFinalize() // Destroy pipeline and shutdown native code
    private external fun nativePlay() // Set pipeline to PLAYING
    private external fun nativePause() // Set pipeline to PAUSED
    private val nativeCustomData: Long = 0 // Native code will use this to keep private data:
    private var isPlayingDesired = false // Whether the user asked to go to PLAYING


    // Called when the activity is first created.
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize GStreamer and warn if it fails
        try {
            init(this)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        setContentView(R.layout.main)
        val play = findViewById<View>(R.id.button_play) as ImageButton
        play.setOnClickListener {
            isPlayingDesired = true
            nativePlay()
        }
        val pause = findViewById<View>(R.id.button_stop) as ImageButton
        pause.setOnClickListener {
            isPlayingDesired = false
            nativePause()
        }
        if (savedInstanceState != null) {
            isPlayingDesired = savedInstanceState.getBoolean("playing")
            Log.i("GStreamer", "Activity created. Saved state is playing:$isPlayingDesired")
        } else {
            isPlayingDesired = false
            Log.i("GStreamer", "Activity created. There is no saved state, playing: false")
        }

        // Start with disabled buttons, until native code is initialized
        findViewById<View>(R.id.button_play).isEnabled = false
        findViewById<View>(R.id.button_stop).isEnabled = false
        nativeInit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("GStreamer", "Saving state, playing:$isPlayingDesired")
        outState.putBoolean("playing", isPlayingDesired)
    }

    override fun onDestroy() {
        nativeFinalize()
        super.onDestroy()
    }

    // Called from native code. This sets the content of the TextView from the UI thread.
    private fun setMessage(message: String) {
        val tv = findViewById<View>(R.id.textview_message) as TextView
        runOnUiThread { tv.text = message }
    }

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private fun onGStreamerInitialized() {
        Log.i("GStreamer", "Gst initialized. Restoring state, playing:$isPlayingDesired")
        // Restore previous playing state
        if (isPlayingDesired) {
            nativePlay()
        } else {
            nativePause()
        }

        // Re-enable buttons, now that GStreamer is initialized
        val activity: Activity = this
        runOnUiThread {
            activity.findViewById<View>(R.id.button_play).isEnabled = true
            activity.findViewById<View>(R.id.button_stop).isEnabled =
                true
        }
    }

    companion object {
        @JvmStatic
        private external fun nativeClassInit(): Boolean // Initialize native class: cache Method IDs for callbacks

        init {
            System.loadLibrary("gstreamer_android")
            System.loadLibrary("tutorial-2")
            nativeClassInit()
        }
    }
}