package com.example.exploreai.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exploreai.Conversation
import com.example.exploreai.R
import com.example.exploreai.assistant.assistant
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConversationAdapter(
    private val conversations: List<Conversation>,
    private val onItemClick: (Conversation) -> Unit // Lambda for click handling
) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val startTextView: TextView = itemView.findViewById(R.id.sessionStartTextView)
        val destinationTextView: TextView = itemView.findViewById(R.id.sessionDestinationTextView)
        val sessionDateTextView: TextView = itemView.findViewById(R.id.sessionDateTextView)
        val sessionTimeTextView: TextView = itemView.findViewById(R.id.sessionTimeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_session, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = conversations[position]
        holder.startTextView.text = conversation.start
        holder.destinationTextView.text = conversation.destination
        holder.sessionDateTextView.text = conversation.date
        holder.sessionTimeTextView.text = conversation.time

        // Set click listener for the entire item view
        holder.itemView.setOnClickListener {
            onItemClick(conversation) // Pass the clicked session to the lambda
        }
    }

    override fun getItemCount(): Int {
        return conversations.size
    }
}

suspend fun <T> Flow<List<T>>.flattenToList(): List<T> {
    return flatMapConcat { it.asFlow() }.toList()
}


class ConversationHistoryListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_history)
        val recyclerList = findViewById<RecyclerView>(R.id.conversationListView)
        val ctx = this
        lifecycleScope.launch(Dispatchers.IO) {
            val conversations = assistant.allConversations // Call your suspending function
            // Switch back to the main thread to update UI or continue execution
            withContext(Dispatchers.Main) {
                // Use the result (sessions) here
                // Create adapter with a click listener
                val adapter = ConversationAdapter(conversations.flattenToList()) { selectedConversation ->
                    // Handle item click here

                    // Example: Navigate to another activity
                    // val intent = Intent(this, SessionDetailActivity::class.java)
                    // intent.putExtra("session_id", selectedSession.id)
                    // startActivity(intent)
                }
                recyclerList.layoutManager = LinearLayoutManager(ctx)
                recyclerList.adapter = adapter
            }
        }

// Set up back navigation in toolbar
        findViewById<MaterialToolbar>(R.id.topAppBar).setNavigationOnClickListener {
            finish() // Go back to the previous screen
        }
    }
}
