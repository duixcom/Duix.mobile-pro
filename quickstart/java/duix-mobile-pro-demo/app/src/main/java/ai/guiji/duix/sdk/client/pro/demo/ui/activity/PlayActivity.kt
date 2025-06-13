package ai.guiji.duix.sdk.client.pro.demo.ui.activity

import ai.guiji.duix.sdk.client.pro.Player
import ai.guiji.duix.sdk.client.pro.demo.databinding.ActivityPlayBinding
import ai.guiji.duix.sdk.client.pro.demo.ui.adapter.MotionAdapter
import ai.guiji.duix.sdk.client.pro.render.DUIXRenderer
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import org.json.JSONObject


/**
 * 通过自行推送PCM音频流驱动数字人嘴形
 */
class PlayActivity : BaseActivity() {

    companion object {
        const val GL_CONTEXT_VERSION = 2
    }

    private lateinit var modelPath: String

    private lateinit var binding: ActivityPlayBinding
    private var player: Player? = null
    private var mMute: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keepScreenOn()
        fullscreen()
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 启动会话需要的基本信息
        modelPath = intent.getStringExtra("modelPath") ?: ""

        Glide.with(mContext).load("file:///android_asset/bg/bg1.png").into(binding.ivBg)

        val renderer =
            DUIXRenderer(binding.glTextureView)
        binding.glTextureView.setEGLContextClientVersion(GL_CONTEXT_VERSION)
        binding.glTextureView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        binding.glTextureView.isOpaque = false           // 透明
        binding.glTextureView.setRenderer(renderer)
        binding.glTextureView.renderMode =
            GLSurfaceView.RENDERMODE_WHEN_DIRTY      // 一定要在设置完Render之后再调用

        player = Player(mContext, renderer, playerCallback)
        player?.init(modelPath)

        binding.btnPlayAudio1.setOnClickListener {
            playPCM("pcm/1.pcm")
        }
        binding.btnPlayAudio2.setOnClickListener {
            playPCM("pcm/3.pcm")
        }
        binding.btnMute.setOnClickListener {
            mMute = !mMute
            binding.btnMute.text = if (mMute) "恢复音量" else "静音"
            player?.setVolume(if (mMute) 0.0F else 1.0F)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }

    private fun playPCM(assetPath: String){
        Log.e("123", "============= playPCM")
        val thread = Thread {
            player?.startPush()
            val inputStream = assets.open(assetPath)
            val buffer = ByteArray(1280)
            while (inputStream.read(buffer) != -1){
                val data = buffer.clone()
                player?.pushPcm(data)
            }
            player?.stopPush()
        }
        thread.start()
    }

    private val playerCallback = object : Player.Callback {
        override fun onInitSuccess(modelInfo: String) {
            runOnUiThread {
                Log.e(TAG, "onInitSuccess modelInfo: $modelInfo")
                Toast.makeText(mContext, "Init success", Toast.LENGTH_SHORT).show()
                binding.btnPlayAudio1.isEnabled = true
                binding.btnPlayAudio2.isEnabled = true
                binding.btnMute.isEnabled = true

                val modelJson = JSONObject(modelInfo)
                val motionJSONArray = modelJson.getJSONArray("motion")
                if (motionJSONArray.length() > 0){
                    val names = ArrayList<String>()
                    for (index in 0 until motionJSONArray.length()){
                        names.add(motionJSONArray.getString(index))
                    }
                    val motionAdapter = MotionAdapter(names, object : MotionAdapter.Callback{
                        override fun onClick(name: String, now: Boolean) {
                            player?.requireMotion(name, now)
                        }
                    })
                    binding.rvMotion.adapter = motionAdapter
                    binding.tvMotionTips.visibility = View.VISIBLE
                }
            }
        }

        override fun onInitError(code: Int, subCode: Int, msg: String?) {
            runOnUiThread {
                Toast.makeText(mContext, "Init error $code $subCode, $msg", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        override fun onPlayStart() {
            Log.e("123", "============= onPlayStart")
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

        override fun onRenderReport(resultCode: Int, isLip: Boolean, useTime: Long) {
            super.onRenderReport(resultCode, isLip, useTime)
            Log.e(TAG, "onRenderReport resultCode: $resultCode isLip: $isLip useTime: $useTime")
        }
    }


}
