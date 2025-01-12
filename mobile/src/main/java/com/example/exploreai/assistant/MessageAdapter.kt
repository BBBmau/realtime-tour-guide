package com.example.exploreai.assistant

import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.exploreai.R

data class Message(
    val text: String,
    val isFromUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private val messages = mutableListOf<Message>()

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
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

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}