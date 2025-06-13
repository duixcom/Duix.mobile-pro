package ai.guiji.duix.sdk.client.pro.demo.ui.activity

import ai.guiji.duix.sdk.client.pro.BuildConfig
import ai.guiji.duix.sdk.client.pro.VirtualFactory
import ai.guiji.duix.sdk.client.pro.demo.databinding.ActivityMainBinding
import ai.guiji.duix.sdk.client.pro.demo.ui.dialog.LoadingDialog
import ai.guiji.duix.sdk.client.pro.VirtualModelUtil
import ai.guiji.duix.sdk.client.pro.callback.ModelDownloadCallback
import ai.guiji.duix.sdk.client.pro.demo.R
import ai.guiji.duix.sdk.client.pro.demo.util.ACache
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import java.io.File


class MainActivity : BaseActivity() {

    companion object {
        const val PLAY_TYPE_COMMON = 0
        const val PLAY_TYPE_LIVE_KIT = 1
    }

    private lateinit var binding: ActivityMainBinding
    private val mLoadingDialog by lazy { LoadingDialog(mContext) }
    private var mLastProgress = 0

    private var mConversationId = ""
    private var mBaseConfigPath = ""         // 临时存放下载模型地址或模型名称
    private var mModelPath = ""         // 临时存放下载模型地址或模型名称

    private var mPlayType = PLAY_TYPE_COMMON

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvSdkVersion.text = "SDK Version: ${BuildConfig.VERSION_NAME}"

        binding.btnPlay.setOnClickListener {
            val appId = binding.etAppId.text.toString()
            val appKey = binding.etAppKey.text.toString()
            mConversationId = binding.etConversationId.text.toString()
            mBaseConfigPath = binding.etBaseConfigUrl.text.toString()
            mModelPath = binding.etModelUrl.text.toString()

            ACache.get(mContext).put("appId", appId)
            ACache.get(mContext).put("appKey", appKey)
            ACache.get(mContext).put("conversationId", mConversationId)
            ACache.get(mContext).put("baseConfigPath", mBaseConfigPath)
            ACache.get(mContext).put("modelPath", mModelPath)

            VirtualFactory.init(this, appId, appKey)
//            VirtualFactory.setHostname("prevshow.guiji.ai")
            mPlayType = PLAY_TYPE_COMMON
            checkModel()
        }

        binding.btnLiveKit.setOnClickListener {
            val appId = binding.etAppId.text.toString()
            val appKey = binding.etAppKey.text.toString()
            mConversationId = binding.etConversationId.text.toString()
            mBaseConfigPath = binding.etBaseConfigUrl.text.toString()
            mModelPath = binding.etModelUrl.text.toString()
            if (TextUtils.isEmpty(mConversationId)){
                Toast.makeText(mContext, "请提供会话ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ACache.get(mContext).put("appId", appId)
            ACache.get(mContext).put("appKey", appKey)
            ACache.get(mContext).put("conversationId", mConversationId)
            ACache.get(mContext).put("baseConfigPath", mBaseConfigPath)
            ACache.get(mContext).put("modelPath", mModelPath)


            VirtualFactory.init(this, appId, appKey)
//            VirtualFactory.setHostname("prevshow.guiji.ai")
            mPlayType = PLAY_TYPE_LIVE_KIT
            requestPermission(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        val appId = ACache.get(mContext).getAsString("appId")
        if (!TextUtils.isEmpty(appId)){
            binding.etAppId.setText(appId)
        }
        val appKey = ACache.get(mContext).getAsString("appKey")
        if (!TextUtils.isEmpty(appKey)){
            binding.etAppKey.setText(appKey)
        }
        val conversationId = ACache.get(mContext).getAsString("conversationId")
        if (!TextUtils.isEmpty(conversationId)){
            binding.etConversationId.setText(conversationId)
        }
        val baseConfigPath = ACache.get(mContext).getAsString("baseConfigPath")
        if (!TextUtils.isEmpty(baseConfigPath)){
            binding.etBaseConfigUrl.setText(baseConfigPath)
        }
        val modelPath = ACache.get(mContext).getAsString("modelPath")
        if (!TextUtils.isEmpty(modelPath)){
            binding.etModelUrl.setText(modelPath)
        }
    }

    override fun permissionsGet(get: Boolean, code: Int) {
        super.permissionsGet(get, code)
        if (get){
            if (code == 1){
                checkModel()
            }
        } else {
            Toast.makeText(mContext, R.string.need_permission, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkModel(){
        // 查看模型是否已经下载
        if (VirtualModelUtil.checkModel(mModelPath)){
            checkBaseConfig()
        } else {
            modelDownload()
        }
    }

    private fun checkBaseConfig(){
        if (VirtualModelUtil.checkBaseConfig()){
            if (mLoadingDialog.isShowing){
                mLoadingDialog.dismiss()
            }
            val intent = Intent(mContext, if (mPlayType == PLAY_TYPE_LIVE_KIT) LiveKitActivity::class.java else PlayActivity::class.java)
            intent.putExtra("modelPath", mModelPath)
            intent.putExtra("conversationId", mConversationId)
            startActivity(intent)
        } else {
            baseConfigDownload()
        }
    }

    private fun modelDownload(){
        mLoadingDialog.show()
        VirtualModelUtil.modelDownload(mContext, mModelPath, object :
            ModelDownloadCallback {
            override fun onDownloadProgress(url: String?, current: Long, total: Long) {
                val progress = (current * 100 / total).toInt()
                if (progress != mLastProgress){
                    mLastProgress = progress
                    runOnUiThread {
                        if (mLoadingDialog.isShowing){
                            mLoadingDialog.setContent("Model download(${progress}%)")
                        }
                    }

                }
            }

            override fun onUnzipProgress(url: String?, current: Long, total: Long) {
                val progress = (current * 100 / total).toInt()
                if (progress != mLastProgress){
                    mLastProgress = progress
                    runOnUiThread {
                        if (mLoadingDialog.isShowing){
                            mLoadingDialog.setContent("Model unzip(${progress}%)")
                        }
                    }
                }
            }

            override fun onDownloadComplete(url: String?, dir: File?) {
                runOnUiThread {
                    checkBaseConfig()
                }
            }

            override fun onDownloadFail(url: String?, code: Int, msg: String?) {
                runOnUiThread {
                    if (mLoadingDialog.isShowing){
                        mLoadingDialog.dismiss()
                    }
                    Toast.makeText(mContext, "Model download error: $code $msg", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun baseConfigDownload(){
        mLoadingDialog.show()
        VirtualModelUtil.baseConfigDownload(mContext, mBaseConfigPath, object :
            ModelDownloadCallback {
            override fun onDownloadProgress(url: String?, current: Long, total: Long) {
                val progress = (current * 100 / total).toInt()
                if (progress != mLastProgress){
                    mLastProgress = progress
                    runOnUiThread {
                        if (mLoadingDialog.isShowing){
                            mLoadingDialog.setContent("Config download(${progress}%)")
                        }
                    }

                }
            }

            override fun onUnzipProgress(url: String?, current: Long, total: Long) {
                val progress = (current * 100 / total).toInt()
                if (progress != mLastProgress){
                    mLastProgress = progress
                    runOnUiThread {
                        if (mLoadingDialog.isShowing){
                            mLoadingDialog.setContent("Config unzip(${progress}%)")
                        }
                    }
                }
            }

            override fun onDownloadComplete(url: String?, dir: File?) {
                runOnUiThread {
                    if (mLoadingDialog.isShowing){
                        mLoadingDialog.dismiss()
                    }
                    val intent = Intent(mContext, if (mPlayType == PLAY_TYPE_LIVE_KIT) LiveKitActivity::class.java else PlayActivity::class.java)
                    intent.putExtra("modelPath", mModelPath)
                    intent.putExtra("conversationId", mConversationId)
                    startActivity(intent)
                }
            }

            override fun onDownloadFail(url: String?, code: Int, msg: String?) {
                runOnUiThread {
                    if (mLoadingDialog.isShowing){
                        mLoadingDialog.dismiss()
                    }
                    Toast.makeText(mContext, "BaseConfig download error: $code $msg", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }
}
