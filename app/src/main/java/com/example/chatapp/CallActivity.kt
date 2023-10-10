package com.example.chatapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.chatapp.R
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas

class CallActivity : AppCompatActivity() {
    // Fill the App ID of your project generated on Agora Console.
    private val appId = "9a1086c887ca48c3b82143a6f3c24633"

    // Fill the channel name.
    private val channelName = "channelOne"
    private var frag = 5

    // Fill the temp token generated on Agora Console.
    private val token = "007eJxTYGBx3XCAdWHiFI9tK8wevvcpY74Z737m3vaGOcV7L570DDmjwGCZaGhgYZZsYWGenGhikWycZGFkaGKcaJZmnGxkYmZsvGWmcmpDICODpIYeKyMDBIL4XAzJGYl5eak5/nmpDAwAOBAhXQ=="

    // An integer that identifies the local user.
    private var isLocalConnected = false
    private var isRemoteConnected = false
    private var isTimerRunning = false
    private var timerSeconds = 0
    private var timer: CountDownTimer? = null

    private val uid = 0
    private var isJoined = false
    private var isFrontCamera = false
    private var agoraEngine: RtcEngine? = null
    private var isMuted = false

    //SurfaceView to render local video in a Container.
    private var localSurfaceView: SurfaceView? = null

    //SurfaceView to render Remote video in a Container.
    private var remoteSurfaceView: SurfaceView? = null
    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )
    private fun startTimerIfBothConnected() {
        if (isLocalConnected && isRemoteConnected && !isTimerRunning) {
            try {
                // Start a countdown timer for 30 seconds (adjust as needed)
                timer = object : CountDownTimer(30000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        timerSeconds = (millisUntilFinished / 1000).toInt()
                        // Update your UI to display the remaining time
                        updateTimerUI(timerSeconds)
                    }

                    override fun onFinish() {
                        // Timer finished, do something (e.g., end the call)
                        // You can stop the call or take any other action here
                        // Reset timer-related variables
                        timerSeconds = 0
                        isTimerRunning = false
                        timer = null
                    }
                }.start()

                isTimerRunning = true

            } catch (e: Exception) {
                // Handle any exceptions here, e.g., log the error
                e.printStackTrace()
            }
        }
    }


    private fun checkSelfPermission(): Boolean {
        return !(ContextCompat.checkSelfPermission(
            this,
            REQUESTED_PERMISSIONS[0]
        ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    REQUESTED_PERMISSIONS[1]
                ) != PackageManager.PERMISSION_GRANTED)
    }

    private fun updateTimerUI(seconds: Int) {
        // Update your UI to display the remaining time (e.g., on a TextView)
        val timerTextView = findViewById<TextView>(R.id.timer)
        timerTextView.isVisible=true
        timerTextView.text = "Time remaining: $seconds seconds"
    }


    fun showMessage(message: String?) {
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupVideoSDKEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = IRtcEventHandler
            agoraEngine = RtcEngine.create(config)

            if (agoraEngine != null) {
                // By default, the video module is disabled, call enableVideo to enable it.
                agoraEngine!!.enableVideo()
            } else {
                showMessage("Failed to initialize Agora Engine")
            }
        } catch (e: Exception) {
            showMessage(e.toString())
        }
    }

    private val IRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote host joining the channel to get the uid of the host.

        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("Remote user joined $uid")

            // Stop the timer and reset timer-related variables
            if (timer != null) {
                timer!!.cancel()
                timerSeconds = 0
                isTimerRunning = false
                timer = null
            }

            // Set the remote video view
            runOnUiThread { setupRemoteVideo(uid) }

            isRemoteConnected = true
            startTimerIfBothConnected()
        }



        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
            showMessage("Joined Channel $channel")



        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("Remote user offline $uid $reason")
            runOnUiThread { remoteSurfaceView!!.visibility = View.GONE }
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)
        remoteSurfaceView = SurfaceView(baseContext)
        remoteSurfaceView!!.setZOrderMediaOverlay(true)
        container.addView(remoteSurfaceView)
        agoraEngine!!.setupRemoteVideo(
            VideoCanvas(
                remoteSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                uid
            )
        )
        // Display RemoteSurfaceView.
        remoteSurfaceView!!.setVisibility(View.VISIBLE)
    }




    private fun setupLocalVideo() {
        val container = findViewById<FrameLayout>(R.id.local_video_view_container)
        // Create a SurfaceView object and add it as a child to the FrameLayout.
        localSurfaceView = SurfaceView(baseContext)
        container.addView(localSurfaceView)
        // Call setupLocalVideo with a VideoCanvas having uid set to 0.
        agoraEngine!!.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                0
            )
        )

    }
    fun joinChannel(view: View?) {
        if (checkSelfPermission()) {
            val options = ChannelMediaOptions()

            // For a Video call, set the channel profile as COMMUNICATION.
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            // Display LocalSurfaceView.
            setupLocalVideo()
            localSurfaceView!!.visibility = View.VISIBLE

            // Ensure the camera starts in the off state.
            isFrontCamera = false

            // Start local preview.
            agoraEngine!!.startPreview()

            // Join the channel with a temp token.
            // You need to specify the user ID yourself, and ensure that it is unique in the channel.
            agoraEngine!!.joinChannel(token, channelName, uid, options)
            val localVideoView = findViewById<FrameLayout>(R.id.local_video_view_container)
            localVideoView.visibility = if (isFrontCamera) View.VISIBLE else View.GONE
            agoraEngine!!.enableLocalVideo(isFrontCamera)

            isLocalConnected = true
            val parent=findViewById<ConstraintLayout>(R.id.callParent)
            parent.isVisible=true
            val button=findViewById<Button>(R.id.JoinButton)
            button.isVisible=false

            startTimerIfBothConnected()

        } else {
            Toast.makeText(applicationContext, "Permissions were not granted", Toast.LENGTH_SHORT).show()
        }
    }


    fun leaveChannel(view: View?) {
        if (!isJoined) {
            showMessage("Join a channel first")
        } else {
            // Stop the timer and reset timer-related variables
            if (timer != null) {
                timer!!.cancel()
                timerSeconds = 0
                isTimerRunning = false
                timer = null
            }

            // Leave the channel
            agoraEngine!!.leaveChannel()
            showMessage("You left the channel")
//            val parent=findViewById<ConstraintLayout>(R.id.callParent)
//            parent.isVisible=false
//            val button=findViewById<Button>(R.id.JoinButton)
//            button.isVisible=true
            finish()
            // Stop remote video rendering.
            if (remoteSurfaceView != null) remoteSurfaceView!!.visibility = View.GONE
            // Stop local video rendering.
            if (localSurfaceView != null) localSurfaceView!!.visibility = View.GONE
            isJoined = false
        }
    }


    fun toggleCamera(view: View?) {
        // Check if the AgoraEngine object is initialized
        if (agoraEngine != null) {
            // Toggle the camera state and update the button text accordingly.
            isFrontCamera = !isFrontCamera
//            val buttonText = if (isFrontCamera)  else "Camera On"

            // Update the UI button text.
            val cameraOffButton = findViewById<ImageView>(R.id.CameraButton)
            val TvCameraOff = findViewById<TextView>(R.id.tvCameraOff)
//            cameraOffButton.text = buttonText
            if (isFrontCamera){
                cameraOffButton.setImageResource(R.drawable.camera_on)
                TvCameraOff.isVisible=false
            }
            else {cameraOffButton.setImageResource(R.drawable.camera_off)

                TvCameraOff.isVisible=true
            }
            // Use the enableLocalVideo method to enable or disable the local camera.
            agoraEngine!!.enableLocalVideo(isFrontCamera)


            val localVideoView = findViewById<FrameLayout>(R.id.local_video_view_container)
            localVideoView.visibility =View.VISIBLE
        }
    }

    fun flipCamera(view: View?) {
        // Check if the AgoraEngine object is initialized
        if (agoraEngine != null) {
            // Use the switchCamera method to toggle between the front and back cameras.
            agoraEngine!!.switchCamera()
        }
    }

    fun toggleMute(view: View?) {
        // Check if the AgoraEngine object is initialized
        if (agoraEngine != null) {
            // Toggle the mute state and update the button text accordingly.
            isMuted = !isMuted
//            val buttonText = if (isMuted) "Unmute" else "Mute"

            // Update the UI button text.
            val muteButton = findViewById<ImageView>(R.id.MuteButton)
            val localMute = findViewById<ImageView>(R.id.localMute)
//            muteButton.text = buttonText

            if (isMuted) {
                muteButton.setImageResource(R.drawable.mute)
                localMute.setImageResource(R.drawable.mute)
            }  else {
                muteButton.setImageResource(R.drawable.mic)
                localMute.setImageResource(R.drawable.mic)

            }
            // Use the muteLocalAudioStream method to mute or unmute the microphone.
            agoraEngine!!.muteLocalAudioStream(isMuted)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        frag=intent.getIntExtra("frag",5)
        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
        }
        if(frag==0){
            isMuted=false
            isFrontCamera=false


        }
        if(frag==1){
            isMuted=false
            isFrontCamera=true
            val localVideoView = findViewById<FrameLayout>(R.id.local_video_view_container)
            localVideoView.visibility = if (isFrontCamera) View.VISIBLE else View.GONE
        }

        setupVideoSDKEngine()


        val options = ChannelMediaOptions()

        // For a Video call, set the channel profile as COMMUNICATION.
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
        // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        // Display LocalSurfaceView.
        setupLocalVideo()
        localSurfaceView!!.visibility = View.VISIBLE

        // Ensure the camera starts in the off state.
//            isFrontCamera = false

        // Start local preview.
        agoraEngine!!.startPreview()

        // Join the channel with a temp token.
        // You need to specify the user ID yourself, and ensure that it is unique in the channel.
        agoraEngine!!.joinChannel(token, channelName, uid, options)
        val localVideoView = findViewById<FrameLayout>(R.id.local_video_view_container)
        localVideoView.visibility = if (isFrontCamera) View.VISIBLE else View.GONE
        agoraEngine!!.enableLocalVideo(isFrontCamera)
        agoraEngine!!.muteLocalAudioStream(isMuted)
        val muteButton = findViewById<ImageView>(R.id.MuteButton)
        val localMute = findViewById<ImageView>(R.id.localMute)
//            muteButton.text = buttonText

        if (isMuted) {
            muteButton.setImageResource(R.drawable.mute)
            localMute.setImageResource(R.drawable.mute)
        }  else {
            muteButton.setImageResource(R.drawable.mic)
            localMute.setImageResource(R.drawable.mic)

        }
        isLocalConnected = true
//            val parent=findViewById<ConstraintLayout>(R.id.callParent)
//            parent.isVisible=true
//            val button=findViewById<Button>(R.id.JoinButton)
//            button.isVisible=false

        startTimerIfBothConnected()



    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.stopPreview()
        agoraEngine!!.leaveChannel()

        // Destroy the engine in a sub-thread to avoid congestion
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }
    fun startVoiceCall(view: View?) {
//        val intent = Intent(this, VoiceCallActivity::class.java)
//        startActivity(intent)
    }

}