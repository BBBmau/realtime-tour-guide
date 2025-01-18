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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exploreai.R
import com.google.android.material.appbar.MaterialToolbar

data class Session(
    val id: Int,
    val start: String,
    val destination: String,
    val date: String,
    val time: String,
)

class SessionAdapter(
    private val sessions: List<Session>,
    private val onItemClick: (Session) -> Unit // Lambda for click handling
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val startTextView: TextView = itemView.findViewById(R.id.sessionStartTextView)
        val destinationTextView: TextView = itemView.findViewById(R.id.sessionDestinationTextView)
        val sessionDateTextView: TextView = itemView.findViewById(R.id.sessionDateTextView)
        val sessionTimeTextView: TextView = itemView.findViewById(R.id.sessionTimeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        holder.startTextView.text = session.start
        holder.destinationTextView.text = session.destination
        holder.sessionDateTextView.text = session.date
        holder.sessionTimeTextView.text = session.time

        // Set click listener for the entire item view
        holder.itemView.setOnClickListener {
            onItemClick(session) // Pass the clicked session to the lambda
        }
    }

    override fun getItemCount(): Int {
        return sessions.size
    }
}



class ConversationHistoryListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_history)
        val recyclerList = findViewById<RecyclerView>(R.id.conversationListView)
        // Sample data for sessions
        val sessions = listOf(
            Session(1, "La Jolla, CA", "Irvine, CA", "January 16, 2024", "3:00pm"),
            Session(2, "Cathedral City, CA", "La Quinta, CA", "February 26, 2024", "12:00pm"),
            Session(1, "La Jolla, CA", "Irvine, CA", "January 16, 2024", "3:00pm"),
            Session(2, "Cathedral City, CA", "La Quinta, CA", "February 26, 2024", "12:00pm"),
            Session(1, "La Jolla, CA", "Irvine, CA", "January 16, 2024", "3:00pm"),
            Session(2, "Cathedral City, CA", "La Quinta, CA", "February 26, 2024", "12:00pm"),
            Session(1, "La Jolla, CA", "Irvine, CA", "January 16, 2024", "3:00pm"),
            Session(2, "Cathedral City, CA", "La Quinta, CA", "February 26, 2024", "12:00pm"),
            Session(1, "La Jolla, CA", "Irvine, CA", "January 16, 2024", "3:00pm"),
            Session(2, "Cathedral City, CA", "La Quinta, CA", "February 26, 2024", "12:00pm"),
            Session(1, "La Jolla, CA", "Irvine, CA", "January 16, 2024", "3:00pm"),
            Session(2, "Cathedral City, CA", "La Quinta, CA", "February 26, 2024", "12:00pm"),
            Session(1, "La Jolla, CA", "Irvine, CA", "January 16, 2024", "3:00pm"),
            Session(2, "Cathedral City, CA", "La Quinta, CA", "February 26, 2024", "12:00pm"),
            Session(1, "La Jolla, CA", "Irvine, CA", "January 16, 2024", "3:00pm"),
            Session(2, "Cathedral City, CA", "La Quinta, CA", "February 26, 2024", "12:00pm"),
            Session(1, "La Jolla, CA", "Irvine, CA", "January 16, 2024", "3:00pm"),
            Session(2, "Cathedral City, CA", "La Quinta, CA", "February 26, 2024", "12:00pm"),
            Session(1, "La Jolla, CA", "Irvine, CA", "January 16, 2024", "3:00pm"),
            Session(2, "Cathedral City, CA", "La Quinta, CA", "February 26, 2024", "12:00pm"),
//            Session(3, "Session 3", "This is the third session."),
//            Session(1, "Session 1", "This is the first session."),
//            Session(2, "Session 2", "This is the second session."),
//            Session(3, "Session 3", "This is the third session."),
//            Session(1, "Session 1", "This is the first session."),
//            Session(2, "Session 2", "This is the second session."),
//            Session(3, "Session 3", "This is the third session.")
        )

// Create adapter with a click listener
        val adapter = SessionAdapter(sessions) { selectedSession ->
            // Handle item click here
            Toast.makeText(this, "Clicked on ${selectedSession.start} -> ${selectedSession.destination}", Toast.LENGTH_SHORT).show()

            // Example: Navigate to another activity
            // val intent = Intent(this, SessionDetailActivity::class.java)
            // intent.putExtra("session_id", selectedSession.id)
            // startActivity(intent)
        }

        recyclerList.layoutManager = LinearLayoutManager(this)
        recyclerList.adapter = adapter

// Set up back navigation in toolbar
        findViewById<MaterialToolbar>(R.id.topAppBar).setNavigationOnClickListener {
            finish() // Go back to the previous screen
        }
    }
}
