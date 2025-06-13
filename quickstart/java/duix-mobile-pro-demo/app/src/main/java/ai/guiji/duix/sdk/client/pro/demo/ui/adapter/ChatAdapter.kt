package ai.guiji.duix.sdk.client.pro.demo.ui.adapter

import ai.guiji.duix.sdk.client.pro.demo.bean.ChatInfo
import ai.guiji.duix.sdk.client.pro.demo.databinding.ItemChatBotBinding
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 这里我先不管动画了，直接所有选项一起刷新
 */
class ChatAdapter(
    private val mContext: Context,
    private val mLayoutManager: LinearLayoutManager
) : RecyclerView.Adapter<ChatAdapter.ChatHolder>() {

    private val mList = ArrayList<ChatInfo>()


    fun clearInfo() {
        if (mList.size > 0) {
            val len = mList.size
            mList.clear()
            notifyItemRangeRemoved(0, len)
        }
    }

    fun addInfo(info: ChatInfo) {
        mList.add(info)
        notifyItemInserted(mList.size - 1)
        mLayoutManager.scrollToPosition(itemCount - 1)
    }

    fun refreshBotContent(content: String) {
        for (index in mList.size -1 downTo 0){
            if (mList[index].type == ChatInfo.TYPE_BOT){
                mList[index].content = content
                notifyItemChanged(index)
                mLayoutManager.scrollToPosition(itemCount - 1)
                break
            }
        }
    }

    fun refreshUserContent(content: String) {
        for (index in mList.size -1 downTo 0){
            if (mList[index].type == ChatInfo.TYPE_USER){
                mList[index].content = content
                notifyItemChanged(index)
                break
            }
        }
    }

    class ChatHolder(val itemBinding: ItemChatBotBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val itemBinding =
            ItemChatBotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        if (position < mList.size){
            mList[position].apply {
                if (this.type == ChatInfo.TYPE_BOT) {
                    holder.itemBinding.tvBot.visibility = View.VISIBLE
                    holder.itemBinding.tvUser.visibility = View.GONE
                    if (TextUtils.isEmpty(content)) {
                        holder.itemBinding.tvBot.text = "......"
                    } else {
                        holder.itemBinding.tvBot.text = content
                    }
                } else {
                    holder.itemBinding.tvBot.visibility = View.GONE
                    holder.itemBinding.tvUser.visibility = View.VISIBLE
                    if (TextUtils.isEmpty(content)) {
//                        Recognizing...
                        holder.itemBinding.tvUser.text = "识别中..."
                    } else {
                        holder.itemBinding.tvUser.text = content
                    }
                }
            }
        } else {
            holder.itemBinding.tvBot.visibility = View.GONE
            holder.itemBinding.tvUser.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return mList.size + 1
    }


}