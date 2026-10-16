package us.zoom.uitoolkitsample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import us.zoom.sdk.IncomingLiveStreamStatus
import us.zoom.sdk.RealTimeMediaStreamsFailReason
import us.zoom.sdk.RealTimeMediaStreamsStatus
import us.zoom.sdk.SubSessionKit
import us.zoom.sdk.SubSessionUserHelpRequestHandler
import us.zoom.sdk.UVCCameraStatus
import us.zoom.sdk.ZoomVideoSDK
import us.zoom.sdk.ZoomVideoSDKAnnotationHelper
import us.zoom.sdk.ZoomVideoSDKAnnotationToolType
import us.zoom.sdk.ZoomVideoSDKAudioHelper
import us.zoom.sdk.ZoomVideoSDKAudioRawData
import us.zoom.sdk.ZoomVideoSDKBroadcastControlStatus
import us.zoom.sdk.ZoomVideoSDKCRCCallStatus
import us.zoom.sdk.ZoomVideoSDKCameraControlRequestHandler
import us.zoom.sdk.ZoomVideoSDKCameraControlRequestType
import us.zoom.sdk.ZoomVideoSDKChatHelper
import us.zoom.sdk.ZoomVideoSDKChatMessage
import us.zoom.sdk.ZoomVideoSDKChatMessageDeleteType
import us.zoom.sdk.ZoomVideoSDKChatPrivilegeType
import us.zoom.sdk.ZoomVideoSDKDataType
import us.zoom.sdk.ZoomVideoSDKDelegate
import us.zoom.sdk.ZoomVideoSDKEmojiReactionType
import us.zoom.sdk.ZoomVideoSDKExportFormat
import us.zoom.sdk.ZoomVideoSDKFileTransferStatus
import us.zoom.sdk.ZoomVideoSDKLiveStreamHelper
import us.zoom.sdk.ZoomVideoSDKLiveStreamStatus
import us.zoom.sdk.ZoomVideoSDKLiveTranscriptionHelper
import us.zoom.sdk.ZoomVideoSDKMultiCameraStreamStatus
import us.zoom.sdk.ZoomVideoSDKNetworkStatus
import us.zoom.sdk.ZoomVideoSDKOnHoldHelper
import us.zoom.sdk.ZoomVideoSDKOnHoldUser
import us.zoom.sdk.ZoomVideoSDKPasswordHandler
import us.zoom.sdk.ZoomVideoSDKPhoneFailedReason
import us.zoom.sdk.ZoomVideoSDKPhoneStatus
import us.zoom.sdk.ZoomVideoSDKProxySettingHandler
import us.zoom.sdk.ZoomVideoSDKQOSStatistics
import us.zoom.sdk.ZoomVideoSDKRawDataPipe
import us.zoom.sdk.ZoomVideoSDKReceiveFile
import us.zoom.sdk.ZoomVideoSDKRecordingConsentHandler
import us.zoom.sdk.ZoomVideoSDKRecordingStatus
import us.zoom.sdk.ZoomVideoSDKSSLCertificateInfo
import us.zoom.sdk.ZoomVideoSDKSendFile
import us.zoom.sdk.ZoomVideoSDKSessionLeaveReason
import us.zoom.sdk.ZoomVideoSDKShareAction
import us.zoom.sdk.ZoomVideoSDKShareHelper
import us.zoom.sdk.ZoomVideoSDKShareSetting
import us.zoom.sdk.ZoomVideoSDKShareStatus
import us.zoom.sdk.ZoomVideoSDKStreamingJoinStatus
import us.zoom.sdk.ZoomVideoSDKSubSessionManager
import us.zoom.sdk.ZoomVideoSDKSubSessionParticipant
import us.zoom.sdk.ZoomVideoSDKSubSessionStatus
import us.zoom.sdk.ZoomVideoSDKTestMicStatus
import us.zoom.sdk.ZoomVideoSDKUser
import us.zoom.sdk.ZoomVideoSDKUserHelpRequestResult
import us.zoom.sdk.ZoomVideoSDKUserHelper
import us.zoom.sdk.ZoomVideoSDKVirtualBackgroundDataType
import us.zoom.sdk.ZoomVideoSDKVideoCanvas
import us.zoom.sdk.ZoomVideoSDKVideoHelper
import us.zoom.sdk.ZoomVideoSDKVideoSubscribeFailReason
import us.zoom.sdk.ZoomVideoSDKVideoView
import us.zoom.sdk.ZoomVideoSDKWhiteboardHelper
import us.zoom.uitoolkit.ZMUIToolKitAudioBtn
import us.zoom.uitoolkit.ZMUIToolKitParticipantsBtn
import us.zoom.uitoolkit.ZMUIToolKitLeaveBtn
import us.zoom.uitoolkit.ZMUIToolKitMoreBtn
import us.zoom.uitoolkit.ZMUIToolKitShareBtn
import us.zoom.uitoolkit.ZMUIToolKitSwitchCameraBtn
import us.zoom.uitoolkit.ZMUIToolKitVideoBtn
import us.zoom.uitoolkit.ZMUIToolkitVideoPage
import us.zoom.uitoolkit.manager.ZMUIToolKitManager
import us.zoom.uitoolkit.manager.ZMUIToolKitPiPHelper
import us.zoom.uitoolkit.manager.ZMUIToolKitVirtualBackgroundManager
import us.zoom.uitoolkitsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ZoomVideoSDKDelegate {

    companion object {
        const val TAG = "MainActivity"
        private const val SAMPLE_THUMBNAIL_MAX_EDGE = 512
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var videoBtn: ZMUIToolKitVideoBtn
    private lateinit var audioBtn: ZMUIToolKitAudioBtn
    private lateinit var shareBtn: ZMUIToolKitShareBtn
    private lateinit var leaveBtn: ZMUIToolKitLeaveBtn
    private lateinit var backBtn: ImageButton
    private lateinit var switchCameraBtn: ZMUIToolKitSwitchCameraBtn
    private lateinit var participantsBtn: ZMUIToolKitParticipantsBtn
    private lateinit var moreBtn: ZMUIToolKitMoreBtn
    private lateinit var sessionNameTextView: TextView
    private lateinit var videoPage: ZMUIToolkitVideoPage
    private lateinit var connectingText: TextView
    private var sampleVirtualBackgroundsRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UIToolKitApp.instance.ensureToolkitAttached()
        // Must be before setContentView so SurfaceViews position correctly in edge-to-edge mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        ZoomVideoSDK.getInstance().addListener(this)
        videoBtn = binding.videoBtn
        audioBtn = binding.audioBtn
        shareBtn = binding.shareBtn
        leaveBtn = binding.leaveBtn
        backBtn = binding.backBtn
        switchCameraBtn = binding.switchCameraBtn
        sessionNameTextView = binding.sessionNameText
        participantsBtn = binding.participantsBtn
        moreBtn = binding.moreBtn
        // Default is ALL (Settings, Chat, VB, recording, share). Do not permanently
        // restrict the sample — it has no dedicated Chat/VB/recording toolbar buttons.
        // Example (docs Example 5) — include only Chat + Settings:
        // moreBtn.setIncludedItems(
        //     ZMUIToolKitMoreMenuItems.CHAT or ZMUIToolKitMoreMenuItems.SETTINGS
        // )
        // Tip: use `or` to combine flags. `CHAT and SETTINGS` is 0 (Cancel only).
        videoPage = binding.videoPage
        connectingText = binding.connectingText
        val inSession = ZMUIToolKitManager.sharedInstance().isInSession()
        videoBtn.visibility = if (inSession) View.VISIBLE else View.GONE
        audioBtn.visibility = if (inSession) View.VISIBLE else View.GONE
        shareBtn.visibility = if (inSession) View.VISIBLE else View.GONE
        leaveBtn.visibility = if (inSession) View.VISIBLE else View.GONE
        switchCameraBtn.visibility = if (inSession) View.VISIBLE else View.GONE
        participantsBtn.visibility = if (inSession) View.VISIBLE else View.GONE
        moreBtn.visibility = if (inSession) View.VISIBLE else View.GONE
        connectingText.visibility = if (inSession) View.GONE else View.VISIBLE
        if (inSession) {
            sessionNameTextView.text = ZoomVideoSDK.getInstance().session?.sessionName
            registerSampleVirtualBackgrounds()
        }
        backBtn.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        setContentView(binding.root)
        // Runs after ZMUIToolKitBtn's own post{} so Material's 24dp side padding
        // cannot push Audio / More back off the screen.
        binding.bottomControlBar.post { tightenToolbarButtons() }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            applyWindowInsets(windowInsets)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            ViewCompat.getRootWindowInsets(binding.root)?.let { applyWindowInsets(it) }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Hide chrome and force a synchronous layout so the PiP snapshot doesn't
        // include the top/bottom bars. onResume restores them.
        binding.topBar.visibility = View.GONE
        binding.bottomControlBar.visibility = View.GONE
        val root = binding.root
        if (root.width > 0 && root.height > 0) {
            root.measure(
                View.MeasureSpec.makeMeasureSpec(root.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(root.height, View.MeasureSpec.EXACTLY),
            )
            root.layout(root.left, root.top, root.right, root.bottom)
        }
        ZMUIToolKitPiPHelper.onUserLeaveHint(this)
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !isInPictureInPictureMode) {
            binding.topBar.visibility = View.VISIBLE
            binding.bottomControlBar.visibility = View.VISIBLE
        }
    }

    private fun tightenToolbarButtons() {
        val horizontalPadding = (2 * resources.displayMetrics.density).toInt()
        listOf(audioBtn, videoBtn, shareBtn, participantsBtn, moreBtn).forEach { button ->
            button.minWidth = 0
            button.minimumWidth = 0
            button.setPadding(
                horizontalPadding,
                button.paddingTop,
                horizontalPadding,
                button.paddingBottom
            )
        }
    }

    private fun applyWindowInsets(windowInsets: WindowInsetsCompat) {
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        binding.topBar.updatePadding(top = insets.top)

        // The bar background stays full-bleed against the screen edge. Only the
        // buttons are padded, so labels clear the rounded corner without a gap
        // between the bar and the display border.
        val leftPad = maxOf(
            insets.left,
            bottomCornerContentPadding(windowInsets, android.view.RoundedCorner.POSITION_BOTTOM_LEFT)
        )
        val rightPad = maxOf(
            insets.right,
            bottomCornerContentPadding(windowInsets, android.view.RoundedCorner.POSITION_BOTTOM_RIGHT)
        )
        binding.bottomControlBar.updatePadding(left = leftPad, right = rightPad, bottom = insets.bottom)
        binding.bottomControlBar.post {
            binding.bottomControlBar.clipChildren = true
            binding.bottomControlBar.clipToPadding = true
        }
    }

    /** Half the corner radius keeps labels off the curve without narrowing the bar. */
    private fun bottomCornerContentPadding(windowInsets: WindowInsetsCompat, position: Int): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return 0
        val fromInsets = windowInsets.toWindowInsets()?.getRoundedCorner(position)?.radius ?: 0
        val radius = if (fromInsets > 0) {
            fromInsets
        } else {
            display?.getRoundedCorner(position)?.radius ?: 0
        }
        return radius / 2
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (i in permissions.indices) {
            if (Manifest.permission.CAMERA == permissions[i] &&
                grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                videoBtn.visibility = View.VISIBLE
            }
            if (Manifest.permission.RECORD_AUDIO == permissions[i] &&
                grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                audioBtn.visibility = View.VISIBLE
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ZMUIToolKitManager.REQUEST_SHARE_SCREEN_PERMISSION) {
            Log.d(TAG, "onActivityResult REQUEST_SHARE_SCREEN_PERMISSION")
            if (resultCode != RESULT_OK) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onActivityResult REQUEST_SHARE_SCREEN_PERMISSION no ok ")
                return
            }
            ZMUIToolKitManager.sharedInstance().startShareScreen(this, data!!, MainActivity::class.java)
        }
    }

    private fun requestPermission(): Boolean {
        var permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT >= 34) {
            permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_PHONE_STATE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_PHONE_STATE
            )
        } else if (Build.VERSION.SDK_INT >= 31) {
            permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
            )
        }
        for (permission in permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 1010)
                return false
            } else {
                when (permission) {
                    Manifest.permission.CAMERA -> videoBtn.visibility = View.VISIBLE
                    Manifest.permission.RECORD_AUDIO -> audioBtn.visibility = View.VISIBLE
                }
            }
        }
        return true
    }

    // region ZoomVideoSDKDelegate

    override fun onSessionJoin() {
        registerSampleVirtualBackgrounds()
        connectingText.visibility = View.GONE
        requestPermission()
        shareBtn.visibility = View.VISIBLE
        leaveBtn.visibility = View.VISIBLE
        switchCameraBtn.visibility = View.VISIBLE
        participantsBtn.visibility = View.VISIBLE
        sessionNameTextView.text = ZoomVideoSDK.getInstance().session?.sessionName
        moreBtn.visibility = View.VISIBLE
    }

    override fun onSessionLeave(reason: ZoomVideoSDKSessionLeaveReason?) {
        sampleVirtualBackgroundsRegistered = false
        val intent = Intent(this, JoinActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    override fun onError(errorCode: Int) {}
    override fun onUserJoin(userHelper: ZoomVideoSDKUserHelper?, userList: List<ZoomVideoSDKUser?>?) {}
    override fun onUserLeave(userHelper: ZoomVideoSDKUserHelper?, userList: List<ZoomVideoSDKUser?>?) {}
    override fun onHoldJoined() {}
    override fun onHoldUserJoin(
        onHoldHelper: ZoomVideoSDKOnHoldHelper?,
        userList: List<ZoomVideoSDKOnHoldUser?>?
    ) {}
    override fun onHoldUserLeave(
        onHoldHelper: ZoomVideoSDKOnHoldHelper?,
        userList: List<ZoomVideoSDKOnHoldUser?>?
    ) {}

    override fun onUserVideoStatusChanged(videoHelper: ZoomVideoSDKVideoHelper?, userList: List<ZoomVideoSDKUser?>?) {}
    override fun onUserAudioStatusChanged(audioHelper: ZoomVideoSDKAudioHelper?, userList: List<ZoomVideoSDKUser?>?) {}
    override fun onUserShareStatusChanged(shareHelper: ZoomVideoSDKShareHelper?, userInfo: ZoomVideoSDKUser?, status: ZoomVideoSDKShareStatus?) {}
    override fun onUserShareStatusChanged(shareHelper: ZoomVideoSDKShareHelper?, userInfo: ZoomVideoSDKUser?, shareAction: ZoomVideoSDKShareAction?) {}
    override fun onShareContentChanged(shareHelper: ZoomVideoSDKShareHelper?, userInfo: ZoomVideoSDKUser?, shareAction: ZoomVideoSDKShareAction?) {}
    override fun onLiveStreamStatusChanged(liveStreamHelper: ZoomVideoSDKLiveStreamHelper?, status: ZoomVideoSDKLiveStreamStatus?) {}
    override fun onChatNewMessageNotify(chatHelper: ZoomVideoSDKChatHelper?, messageItem: ZoomVideoSDKChatMessage?) {}
    override fun onChatDeleteMessageNotify(chatHelper: ZoomVideoSDKChatHelper?, msgID: String?, deleteBy: ZoomVideoSDKChatMessageDeleteType?) {}
    override fun onChatPrivilegeChanged(chatHelper: ZoomVideoSDKChatHelper?, currentPrivilege: ZoomVideoSDKChatPrivilegeType?) {}
    override fun onUserHostChanged(userHelper: ZoomVideoSDKUserHelper?, userInfo: ZoomVideoSDKUser?) {}
    override fun onUserManagerChanged(user: ZoomVideoSDKUser?) {}
    override fun onUserNameChanged(user: ZoomVideoSDKUser?) {}
    override fun onUserFailoverStatusChanged(user: ZoomVideoSDKUser?, isInFailover: Boolean) {}
    override fun onUserActiveAudioChanged(audioHelper: ZoomVideoSDKAudioHelper?, list: List<ZoomVideoSDKUser?>?) {}
    override fun onSessionNeedPassword(handler: ZoomVideoSDKPasswordHandler?) {}
    override fun onSessionPasswordWrong(handler: ZoomVideoSDKPasswordHandler?) {}
    override fun onMixedAudioRawDataReceived(rawData: ZoomVideoSDKAudioRawData?) {}
    override fun onOneWayAudioRawDataReceived(rawData: ZoomVideoSDKAudioRawData?, user: ZoomVideoSDKUser?) {}
    override fun onShareAudioRawDataReceived(rawData: ZoomVideoSDKAudioRawData?) {}
    override fun onCommandReceived(sender: ZoomVideoSDKUser?, strCmd: String?) {}
    override fun onCommandChannelConnectResult(isSuccess: Boolean) {}
    override fun onCloudRecordingStatus(status: ZoomVideoSDKRecordingStatus?, handler: ZoomVideoSDKRecordingConsentHandler?) {}
    override fun onHostAskUnmute() {}
    override fun onInviteByPhoneStatus(status: ZoomVideoSDKPhoneStatus?, reason: ZoomVideoSDKPhoneFailedReason?) {}
    override fun onMultiCameraStreamStatusChanged(status: ZoomVideoSDKMultiCameraStreamStatus?, user: ZoomVideoSDKUser?, videoPipe: ZoomVideoSDKRawDataPipe?) {}
    override fun onMultiCameraStreamStatusChanged(status: ZoomVideoSDKMultiCameraStreamStatus?, user: ZoomVideoSDKUser?, canvas: ZoomVideoSDKVideoCanvas?) {}
    override fun onLiveTranscriptionStatus(status: ZoomVideoSDKLiveTranscriptionHelper.ZoomVideoSDKLiveTranscriptionStatus?) {}
    override fun onOriginalLanguageMsgReceived(messageInfo: ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionMessageInfo?) {}
    override fun onLiveTranscriptionMsgInfoReceived(messageInfo: ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionMessageInfo?) {}
    override fun onLiveTranscriptionMsgError(spokenLanguage: ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionLanguage?, transcriptLanguage: ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionLanguage?) {}
    override fun onSpokenLanguageChanged(spokenLanguage: ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionLanguage?) {}
    override fun onVoiceInterpretationReady() {}
    override fun onProxySettingNotification(handler: ZoomVideoSDKProxySettingHandler?) {}
    override fun onSSLCertVerifiedFailNotification(info: ZoomVideoSDKSSLCertificateInfo?) {}
    override fun onCameraControlRequestResult(user: ZoomVideoSDKUser?, isApproved: Boolean) {}
    override fun onCameraControlRequestReceived(user: ZoomVideoSDKUser?, requestType: ZoomVideoSDKCameraControlRequestType?, requestHandler: ZoomVideoSDKCameraControlRequestHandler?) {}
    override fun onUserRecordingConsent(user: ZoomVideoSDKUser?) {}
    override fun onCallCRCDeviceStatusChanged(status: ZoomVideoSDKCRCCallStatus?) {}
    override fun onVideoCanvasSubscribeFail(fail_reason: ZoomVideoSDKVideoSubscribeFailReason?, pUser: ZoomVideoSDKUser?, view: ZoomVideoSDKVideoView?) {}
    override fun onShareCanvasSubscribeFail(fail_reason: ZoomVideoSDKVideoSubscribeFailReason?, pUser: ZoomVideoSDKUser?, view: ZoomVideoSDKVideoView?) {}
    override fun onShareCanvasSubscribeFail(pUser: ZoomVideoSDKUser?, view: ZoomVideoSDKVideoView?, shareAction: ZoomVideoSDKShareAction?) {}
    override fun onAnnotationHelperCleanUp(helper: ZoomVideoSDKAnnotationHelper?) {}
    override fun onAnnotationPrivilegeChange(shareOwner: ZoomVideoSDKUser?, shareAction: ZoomVideoSDKShareAction?) {}
    override fun onAnnotationToolTypeChanged(helper: ZoomVideoSDKAnnotationHelper?, view: ZoomVideoSDKVideoView?, toolType: ZoomVideoSDKAnnotationToolType?) {}
    override fun onTestMicStatusChanged(status: ZoomVideoSDKTestMicStatus?) {}
    override fun onMicSpeakerVolumeChanged(micVolume: Int, speakerVolume: Int) {}
    override fun onCalloutJoinSuccess(user: ZoomVideoSDKUser?, phoneNumber: String?) {}
    override fun onSendFileStatus(file: ZoomVideoSDKSendFile?, status: ZoomVideoSDKFileTransferStatus?) {}
    override fun onReceiveFileStatus(file: ZoomVideoSDKReceiveFile?, status: ZoomVideoSDKFileTransferStatus?) {}
    override fun onUVCCameraStatusChange(cameraId: String?, status: UVCCameraStatus?) {}
    override fun onVideoAlphaChannelStatusChanged(isAlphaModeOn: Boolean) {}
    override fun onSpotlightVideoChanged(videoHelper: ZoomVideoSDKVideoHelper?, userList: List<ZoomVideoSDKUser?>?) {}
    override fun onFailedToStartShare(shareHelper: ZoomVideoSDKShareHelper?, user: ZoomVideoSDKUser?) {}
    override fun onBindIncomingLiveStreamResponse(bSuccess: Boolean, streamKeyID: String?) {}
    override fun onUnbindIncomingLiveStreamResponse(bSuccess: Boolean, streamKeyID: String?) {}
    override fun onIncomingLiveStreamStatusResponse(bSuccess: Boolean, streamsStatusList: List<IncomingLiveStreamStatus?>?) {}
    override fun onStartIncomingLiveStreamResponse(bSuccess: Boolean, streamKeyID: String?) {}
    override fun onStopIncomingLiveStreamResponse(bSuccess: Boolean, streamKeyID: String?) {}
    override fun onShareContentSizeChanged(shareHelper: ZoomVideoSDKShareHelper?, user: ZoomVideoSDKUser?, shareAction: ZoomVideoSDKShareAction?) {}
    override fun onSubSessionStatusChanged(status: ZoomVideoSDKSubSessionStatus?, subSessionKitList: List<SubSessionKit?>?) {}
    override fun onSubSessionManagerHandle(manager: ZoomVideoSDKSubSessionManager?) {}
    override fun onSubSessionParticipantHandle(participant: ZoomVideoSDKSubSessionParticipant?) {}
    override fun onSubSessionUsersUpdate(subSessionKit: SubSessionKit?) {}
    override fun onBroadcastMessageFromMainSession(message: String?, userName: String?) {}
    override fun onSubSessionUserHelpRequest(handler: SubSessionUserHelpRequestHandler?) {}
    override fun onSubSessionUserHelpRequestResult(eResult: ZoomVideoSDKUserHelpRequestResult?) {}
    override fun onShareSettingChanged(setting: ZoomVideoSDKShareSetting?) {}
    override fun onStartBroadcastResponse(bSuccess: Boolean, channelID: String?) {}
    override fun onStopBroadcastResponse(bSuccess: Boolean) {}
    override fun onGetBroadcastControlStatus(bSuccess: Boolean, status: ZoomVideoSDKBroadcastControlStatus?) {}
    override fun onStreamingJoinStatusChanged(status: ZoomVideoSDKStreamingJoinStatus?) {}
    override fun onEmojiReactionReceived(user: ZoomVideoSDKUser?, type: ZoomVideoSDKEmojiReactionType?) {}
    override fun onUserWhiteboardShareStatusChanged(user: ZoomVideoSDKUser?, helper: ZoomVideoSDKWhiteboardHelper?) {}
    override fun onWhiteboardExported(format: ZoomVideoSDKExportFormat?, data: ByteArray?) {}
    override fun onCanvasSnapshotTaken(user: ZoomVideoSDKUser?, isShare: Boolean) {}
    override fun onCanvasSnapshotIncompatible(user: ZoomVideoSDKUser?) {}
    override fun onQOSStatisticsReceived(statistics: ZoomVideoSDKQOSStatistics?, user: ZoomVideoSDKUser?) {}
    override fun onMyAudioSourceTypeChanged(device: ZoomVideoSDKAudioHelper.ZoomVideoSDKAudioDevice?) {}
    override fun onUserNetworkStatusChanged(type: ZoomVideoSDKDataType?, level: ZoomVideoSDKNetworkStatus?, user: ZoomVideoSDKUser?) {}
    override fun onUserOverallNetworkStatusChanged(level: ZoomVideoSDKNetworkStatus?, user: ZoomVideoSDKUser?) {}
    override fun onAudioLevelChanged(level: Int, audioSharing: Boolean, user: ZoomVideoSDKUser?) {}
    override fun onRealTimeMediaStreamsStatus(status: RealTimeMediaStreamsStatus?) {}
    override fun onRealTimeMediaStreamsFail(failReason: RealTimeMediaStreamsFailReason?) {}

    /**
     * Registers the sample app's bundled backgrounds once the session is ready.
     *
     * [ZoomVideoSDKVirtualBackgroundHelper.addVirtualBackgroundItem] copies the bitmap to a temp
     * JPEG and does not take ownership, so the source bitmap is recycled after every add attempt.
     * The catalogue is in-memory for the session; skip when Image items already exist so
     * `onCreate` + `onSessionJoin` in the same session do not add duplicates.
     */
    private fun registerSampleVirtualBackgrounds() {
        if (sampleVirtualBackgroundsRegistered) return
        val helper = ZoomVideoSDK.getInstance().virtualBackgroundHelper ?: return
        if (!helper.isSupportVirtualBackground) return

        val existingItems = helper.virtualBackgroundItemList.orEmpty()
        if (existingItems.any {
                it?.type == ZoomVideoSDKVirtualBackgroundDataType.ZoomVideoSDKVirtualBackgroundDataType_Image
            }) {
            sampleVirtualBackgroundsRegistered = true
            return
        }
        val manager = ZMUIToolKitVirtualBackgroundManager.sharedInstance()
        listOf(R.drawable.san_francisco, R.drawable.earth, R.drawable.grass).forEach { resourceId ->
            val bitmap = BitmapFactory.decodeResource(resources, resourceId) ?: return@forEach
            val thumbnail = createThumbnail(bitmap)
            val added = try {
                manager.addImage(bitmap, thumbnail) != null
            } catch (_: RuntimeException) {
                false
            }
            if (!added && thumbnail != null && thumbnail !== bitmap && !thumbnail.isRecycled) {
                thumbnail.recycle()
            }
            if (bitmap !== thumbnail && !bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        sampleVirtualBackgroundsRegistered = true
    }

    private fun createThumbnail(source: Bitmap): Bitmap? {
        val width = source.width
        val height = source.height
        if (width <= 0 || height <= 0) return null
        val maxEdge = SAMPLE_THUMBNAIL_MAX_EDGE
        if (width <= maxEdge && height <= maxEdge) {
            return source.copy(source.config ?: Bitmap.Config.ARGB_8888, false)
        }
        val scale = maxEdge.toFloat() / maxOf(width, height)
        val scaledWidth = (width * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
    }

    // endregion

    override fun onDestroy() {
        ZoomVideoSDK.getInstance().removeListener(this)
        super.onDestroy()
        if (isFinishing) {
            ZMUIToolKitManager.sharedInstance().cleanup()
        }
    }
}
