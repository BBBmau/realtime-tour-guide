package com.mau.exploreai.assistant

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mau.exploreai.webrtc.ConversationMessage
import com.mau.exploreai.R

//TODO: we shouldn't need to separate messageAdapters, ideally one should handle both real-time and history messages.
class ConversationMessageAdapter(initialMessages: List<ConversationMessage>) : RecyclerView.Adapter<ConversationMessageAdapter.ConversationMessageViewHolder>() {
    private val messages = initialMessages.toMutableList()

    class ConversationMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationMessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false)
        return ConversationMessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationMessageViewHolder, position: Int) {
        Log.d("[conversationMessageAdapter]", "messages size: ${messages.size}")
        val message = messages[position]
        holder.messageText.text = message.text

        // Set layout parameters for alignment
        val params = holder.messageText.layoutParams as RelativeLayout.LayoutParams

        if (message.isFromUser) {
            // User messages (right side)
            params.addRule(RelativeLayout.ALIGN_PARENT_END)
            holder.messageText.setBackgroundResource(R.drawable.message_bubble_user)
            holder.messageText.setTextColor(Color.WHITE)
        } else {
            // Received messages (left side)
            params.addRule(RelativeLayout.ALIGN_PARENT_START)
            holder.messageText.setBackgroundResource(R.drawable.message_bubble)
            holder.messageText.setTextColor(Color.BLACK)
        }

        // Clear any previous alignment rules that might conflict
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.removeRule(if (message.isFromUser) RelativeLayout.ALIGN_PARENT_START else RelativeLayout.ALIGN_PARENT_END)
        }

        holder.messageText.layoutParams = params
    }

    override fun getItemCount() = messages.size


    fun getAllMessages(): MutableList<ConversationMessage>{
        return messages
    }
}