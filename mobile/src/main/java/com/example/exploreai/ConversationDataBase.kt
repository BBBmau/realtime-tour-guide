package com.example.exploreai

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Database(entities = [Conversation::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessagenDao
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversation")
    fun getAll(): List<Conversation>

    @Insert
    fun insertConversation(conversation: Conversation)

    @Delete
    fun delete(conversation: Conversation)
}

@Entity
data class Conversation(
    @PrimaryKey val conversationId: Int,
    @ColumnInfo(name = "date") val date: String?,
    @ColumnInfo(name = "time") val time: String?,
    @ColumnInfo(name = "start") val start: String?,
    @ColumnInfo(name = "destination") val destination: String?,
)

@Dao
interface MessagenDao {
    @Query("SELECT * FROM message")
    fun getAll(): List<Message>

    @Insert
    fun insertMessage(message: Message)
}

@Entity
data class Message(
    @PrimaryKey val messageId: Int,
    @ColumnInfo(name = "conversation_id") val conversationId: Conversation,
    @ColumnInfo(name = "timestamp") val time: String?,
    @ColumnInfo(name = "is_user") val start: Boolean,
    @ColumnInfo(name = "content") val content: String?,
    )
