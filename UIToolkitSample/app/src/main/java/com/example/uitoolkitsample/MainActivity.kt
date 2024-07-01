package com.example.uitoolkitsample

import android.Manifest
import android.os.Bundle
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import us.zoom.uitoolkit.SessionContext
import us.zoom.uitoolkit.UiToolkitError
import us.zoom.uitoolkit.UiToolkitView

class MainActivity : AppCompatActivity() {
    private lateinit var uiToolkitView: UiToolkitView
    private lateinit var joinButton: Button

    private val uiToolkitListener = object : UiToolkitView.Listener {
        override fun onError(error: UiToolkitError) {
            Toast.makeText(this@MainActivity, error.toString(), Toast.LENGTH_SHORT).show()
        }

        override fun onViewStarted() {
            uiToolkitView.visibility = VISIBLE
        }

        override fun onViewStopped() {
            uiToolkitView.visibility = GONE
        }

    }
    private val buttonClickListener = OnClickListener {
        val sessionContext = SessionContext(Constants.SESSION_NAME, Constants.JWT, Constants.NAME)
        uiToolkitView.joinSession(sessionContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uiToolkitView = findViewById(R.id.ui_toolkit_view)
        joinButton = findViewById(R.id.join_button)

        uiToolkitView.addListener(uiToolkitListener)
        joinButton.setOnClickListener(buttonClickListener)

        requestPermissions()
    }

    private fun requestPermissions() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 0)
    }
}
