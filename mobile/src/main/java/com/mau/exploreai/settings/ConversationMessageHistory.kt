package com.mau.exploreai.settings

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mau.exploreai.databinding.ActivityConversationMessagesBinding
import com.mau.exploreai.AssistantApplication
import com.mau.exploreai.assistant.AssistantViewModel
import com.mau.exploreai.assistant.AssistantViewModelFactory
import com.mau.exploreai.assistant.ConversationMessageAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ConversationMessageHistory : AppCompatActivity() {
    private lateinit var binding: ActivityConversationMessagesBinding
    private lateinit var viewModel: AssistantViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create Repository directly (this is what you need to adjust based on your app)
        val repository = (application as AssistantApplication).repository
        val factory = AssistantViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AssistantViewModel::class.java]

        // Get the conversation ID from the intent
        val conversationId = intent.getIntExtra("conversation_id", -1)
        Log.d("[messageHistory]", "conversationID: ${conversationId}")
        if (conversationId == -1) {
            // Handle error - no valid conversation ID
            finish()
        }

        // Load messages for this conversation
        loadMessagesForConversation(conversationId)
        setupListeners()
    }

    private fun loadMessagesForConversation(conversationId: Int) {
        lifecycleScope.launch {
            Log.d("[messageHistory]", "conversationID: $conversationId")
            viewModel.getMessagesForConversation(conversationId).collectLatest { messages ->
                // Update your UI with the messages
                // For example, set up a RecyclerView adapter with the messages
                Log.d("[messageHistory]", "messageCount: ${messages.size}")
                binding.assistantMessagesHistory.messageList.apply {
                    layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@ConversationMessageHistory)
                    adapter = ConversationMessageAdapter(messages)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }
    }
}