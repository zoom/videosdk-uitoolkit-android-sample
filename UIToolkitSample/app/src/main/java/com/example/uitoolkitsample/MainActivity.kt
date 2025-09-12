package com.example.uitoolkitsample

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import us.zoom.uitoolkit.SessionContext
import us.zoom.uitoolkit.UiToolkitError
import us.zoom.uitoolkit.UiToolkitView
import com.google.gson.annotations.SerializedName

data class JWTOptions(
    @SerializedName("sessionName") val sessionName: String,
    @SerializedName("role") val role: Int,
    @SerializedName("userIdentity") val userIdentity: String,
    @SerializedName("sessionkey") val sessionkey: String,
    @SerializedName("geo_regions") val geo_regions: String,
    @SerializedName("cloud_recording_option") val cloud_recording_option: Int,
    @SerializedName("cloud_recording_election") val cloud_recording_election: Int,
    @SerializedName("telemetry_tracking_id") val telemetry_tracking_id: String,
    @SerializedName("video_webrtc_mode") val video_webrtc_mode: Int,
    @SerializedName("audio_webrtc_mode") val audio_webrtc_mode: Int,
)

class MainActivity : AppCompatActivity() {
    private lateinit var uiToolkitView: UiToolkitView
    private lateinit var joinButton: Button
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var screenCaptureLauncher: ActivityResultLauncher<Intent>

    private val uiToolkitListener = object : UiToolkitView.Listener {
        override fun onError(error: UiToolkitError) {
            Toast.makeText(this@MainActivity, error.toString(), Toast.LENGTH_SHORT).show()
        }

        override fun onShareClicked() {
            println("onShareClicked")
            screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
        }

        override fun onShareEnded() {
            println("onShareEnded")
        }

        override fun onViewStarted() {
            uiToolkitView.visibility = VISIBLE
        }

        override fun onViewStopped() {
            uiToolkitView.visibility = GONE
        }

    }
    private val buttonClickListener = OnClickListener {
        val body = JWTOptions(
            sessionName = Constants.SESSION_NAME,
            role = 1,
            userIdentity = null.toString(),
            sessionkey = null.toString(),
            geo_regions = null.toString(),
            cloud_recording_option = 0,
            cloud_recording_election = 0,
            telemetry_tracking_id = null.toString(),
            video_webrtc_mode = 0,
            audio_webrtc_mode = 0
        )

        if (Constants.SDK_KEY.isNotEmpty() && Constants.SDK_SECRET.isNotEmpty()) {
            val signature: String =
                TokenGenerator.generateToken(body, Constants.SDK_KEY, Constants.SDK_SECRET)
            println(signature)

            val sessionContext =
                SessionContext(Constants.SESSION_NAME, signature, Constants.NAME)
            uiToolkitView.joinSession(sessionContext)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uiToolkitView = findViewById(R.id.ui_toolkit_view)
        joinButton = findViewById(R.id.join_button)

        uiToolkitView.addListener(uiToolkitListener)
        joinButton.setOnClickListener(buttonClickListener)

        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        screenCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    println("Screen capture permission granted!")
                    uiToolkitView.onMediaProjectionResult(data)
                }
            } else {
                println("Screen capture permission denied.")
            }
        }

        requestPermissions()
    }

    private fun requestPermissions() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 0)
    }
}
