package com.example.exploreai

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
import com.google.android.material.appbar.MaterialToolbar

data class Session(
    val id: Int,
    val name: String,
    val description: String
)

class SessionAdapter(
    private val context: Context,
    private val sessions: List<Session>
) : ArrayAdapter<Session>(context, 0, sessions) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_session, parent, false)

        val session = getItem(position)

        val nameTextView = view.findViewById<TextView>(R.id.sessionNameTextView)
        val descriptionTextView = view.findViewById<TextView>(R.id.sessionDescriptionTextView)

        nameTextView.text = session?.name
        descriptionTextView.text = session?.description

        return view
    }
}

class ConversationHistoryListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_history)

        // Sample data for sessions
        val sessions = listOf(
            Session(1, "Session 1", "This is the first session."),
            Session(2, "Session 2", "This is the second session."),
            Session(3, "Session 3", "This is the third session.")
        )

        // Set up ListView with custom adapter
        val listView = findViewById<ListView>(R.id.conversationListView)
        val adapter = SessionAdapter(this, sessions)
        listView.adapter = adapter

        // Handle item clicks
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedSession = sessions[position]
            Toast.makeText(this, "Clicked on ${selectedSession.name}", Toast.LENGTH_SHORT).show()

            // Navigate to another activity or perform actions here
            // Example:
            // val intent = Intent(this, SessionDetailActivity::class.java)
            // intent.putExtra("session_id", selectedSession.id)
            // startActivity(intent)
        }

        // Set up back navigation in toolbar
        findViewById<MaterialToolbar>(R.id.topAppBar).setNavigationOnClickListener {
            finish() // Go back to the previous screen
        }
    }
}
