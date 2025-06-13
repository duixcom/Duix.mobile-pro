package ai.guiji.duix.sdk.client.pro.demo.ui.dialog

import ai.guiji.duix.sdk.client.pro.demo.R
import ai.guiji.duix.sdk.client.pro.demo.databinding.DialogLoadingBinding
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator


class LoadingDialog(private var mContext: Context, private val content: String = "") :
    Dialog(mContext, R.style.dialog_center) {

    private lateinit var binding: DialogLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogLoadingBinding.inflate(layoutInflater)
        super.setContentView(binding.root)

        if (!TextUtils.isEmpty(content)){
            binding.tvContent.text = content
        }

        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    fun setContent(content: String){
        binding.tvContent.text = content
    }

    override fun show() {
        super.show()
        val animation: Animation = AnimationUtils.loadAnimation(mContext, R.anim.rotate)
        val lin = LinearInterpolator()
        animation.interpolator = lin
        binding.ivProgress.startAnimation(animation)
    }

    override fun dismiss() {
        super.dismiss()
        binding.ivProgress.clearAnimation()
    }
}