package ai.guiji.duix.sdk.client.pro.demo.ui.activity

import ai.guiji.duix.sdk.client.pro.Player
import ai.guiji.duix.sdk.client.pro.demo.audio.AudioRecordCore
import ai.guiji.duix.sdk.client.pro.demo.bean.ChatInfo
import ai.guiji.duix.sdk.client.pro.demo.databinding.ActivityLiveKitBinding
import ai.guiji.duix.sdk.client.pro.demo.ui.adapter.ChatAdapter
import ai.guiji.duix.sdk.client.pro.render.DUIXRenderer
import android.content.Context
import android.media.AudioManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import java.nio.ByteBuffer


/**
 * 基于LiveKit实时交互效果演示
 */
class LiveKitActivity : BaseActivity() {

    companion object {
        const val GL_CONTEXT_VERSION = 2
    }

    private lateinit var modelPath: String
    private lateinit var conversationId: String

    private lateinit var binding: ActivityLiveKitBinding
    private var player: Player? = null


    private var audioRecorder: AudioRecordCore? = null
    private var mChatAdapter: ChatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keepScreenOn()
        fullscreen()
        val audioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (!isTablet(mContext)) {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = true
        }
        binding = ActivityLiveKitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 启动会话需要的基本信息
        modelPath = intent.getStringExtra("modelPath") ?: ""
        conversationId = intent.getStringExtra("conversationId") ?: ""

        Glide.with(mContext).load("file:///android_asset/bg/bg1.png").into(binding.ivBg)

        mChatAdapter = ChatAdapter(mContext, binding.rvChat.layoutManager as LinearLayoutManager)
        binding.rvChat.adapter = mChatAdapter
        val itemAnimator = object : DefaultItemAnimator() {
        }
        itemAnimator.supportsChangeAnimations = false
        binding.rvChat.itemAnimator = itemAnimator


        val renderer =
            DUIXRenderer(binding.glTextureView)
        binding.glTextureView.setEGLContextClientVersion(GL_CONTEXT_VERSION)
        binding.glTextureView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        binding.glTextureView.isOpaque = false           // 透明
        binding.glTextureView.setRenderer(renderer)
        binding.glTextureView.renderMode =
            GLSurfaceView.RENDERMODE_WHEN_DIRTY      // 一定要在设置完Render之后再调用


        player = Player(mContext, renderer, playerCallback)
        player?.init(conversationId, modelPath)
        audioRecorder = AudioRecordCore { buffer ->
            player?.sendAudio(buffer)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        audioRecorder?.release()
    }

    private val playerCallback = object : Player.Callback {
        override fun onInitSuccess(modelInfo: String) {
            runOnUiThread {
                audioRecorder?.startRecord()
                Log.e(TAG, "onInitSuccess modelInfo: $modelInfo")
                Toast.makeText(mContext, "Init success", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onInitError(code: Int, subCode: Int, msg: String?) {
            runOnUiThread {
                Toast.makeText(mContext, "Init error $code $subCode, $msg", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        override fun onPlayStart() {
            Log.e(TAG, "onPlayStart")
        }

        override fun onPlayEnd() {
            Log.e(TAG, "onPlayEnd")
        }

        override fun onPlayError(code: Int, msg: String?) {
            Log.e(TAG, "onPlayError code: $code msg: $msg")
        }

        override fun onMotionStart(name: String?) {
            Log.e(TAG, "onMotionStart name: $name")
        }

        override fun onMotionComplete(name: String?) {
            Log.e(TAG, "onMotionComplete name: $name")
        }

        override fun onSpeakStart() {
            runOnUiThread {
                player?.startPush()
                val botMsg = ChatInfo(ChatInfo.TYPE_BOT)
                mChatAdapter?.addInfo(botMsg)
            }
        }

        override fun onSpeakEnd() {
            runOnUiThread {
                player?.stopPush()
            }
        }

        override fun onSpeakBuffer(buffer: ByteBuffer) {
            runOnUiThread {
                val data = ByteArray(buffer.remaining())
                buffer.get(data)
                player?.pushPcm(data)
            }
        }

        override fun onSpeakText(text: String) {
            runOnUiThread {
                mChatAdapter?.refreshBotContent(text)
            }
        }

        override fun onMotion(motion: String?) {
            runOnUiThread {
                Log.e(TAG, "onMotion motion $motion")
                player?.requireMotion(motion)
            }
        }

        override fun onAsrResult(content: String, end: Boolean) {
            runOnUiThread {
                binding.tvASR.text = content
                if (end){
                    binding.tvASR.text = ""
                    val botMsg = ChatInfo(ChatInfo.TYPE_USER)
                    botMsg.content = content
                    mChatAdapter?.addInfo(botMsg)
                }
            }
        }

        override fun onLiveKitConnectError(code: Int, msg: String?) {
            runOnUiThread {
                Toast.makeText(mContext, "LiveKit connect error: $code $msg", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }


}
