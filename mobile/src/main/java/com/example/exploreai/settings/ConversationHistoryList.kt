package com.example.exploreai.settings

import android.content.Context
import android.content.Intent
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
        recyclerList.layoutManager = LinearLayoutManager(ctx)
        
        // Add this to show a loading indicator or message
        Toast.makeText(this, "Loading conversations...", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            try {
                val conversationsFlow = assistant.allConversations
                
                // Collect the flow and update UI when data arrives
                conversationsFlow.collect { conversationsList ->
                    if (conversationsList.isEmpty()) {
                        // Handle empty list case
                        Toast.makeText(ctx, "No conversations found", Toast.LENGTH_SHORT).show()
                    } else {
                        // Create and set adapter with conversations
                        val adapter = ConversationAdapter(conversationsList) { selectedConversation ->
                            // Handle item click here
                            Toast.makeText(ctx, "Selected: ${selectedConversation.start} to ${selectedConversation.destination}",
                                Toast.LENGTH_SHORT).show()

                                startActivity(Intent(ctx, ConversationMessageHistory::class.java))

                            selectedConversation.conversationId
                            // Example: Navigate to another activity with the conversation
                            // val intent = Intent(ctx, ConversationDetailActivity::class.java)
                            // intent.putExtra("conversation_id", selectedConversation.id)
                            // startActivity(intent)
                        }
                        recyclerList.adapter = adapter
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Display error message
                    Toast.makeText(ctx, "Error loading conversations: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }

// Set up back navigation in toolbar
        findViewById<MaterialToolbar>(R.id.topAppBar).setNavigationOnClickListener {
            finish() // Go back to the previous screen
        }
    }
}
