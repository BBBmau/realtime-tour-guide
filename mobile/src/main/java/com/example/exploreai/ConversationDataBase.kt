package com.example.exploreai

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// Annotates class to be a Room Database with a table (entity) of the Conversation class
@Database(entities = [Conversation::class, ConversationMessage::class], version = 2, exportSchema = false)
public abstract class ConversationRoomDatabase : RoomDatabase() {

    abstract fun conversationDAO(): ConversationDao
    abstract fun messageDAO(): MessageDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: ConversationRoomDatabase? = null

        fun getDatabase(context: Context): ConversationRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConversationRoomDatabase::class.java,
                    "conversation_database"
                ).fallbackToDestructiveMigration().build() // TODO: look into migration of database
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversation_table")
    fun getAll(): Flow<List<Conversation>>

    @Insert
    fun insertConversation(conversation: Conversation): Long

    @Delete
    fun delete(conversation: Conversation)
}

@Entity(tableName = "conversation_table")
data class Conversation(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "conversation_id") val conversationId: Int = 0,
    @ColumnInfo(name = "date") val date: String?,
    @ColumnInfo(name = "time") val time: String?,
    @ColumnInfo(name = "start") val start: String?,
    @ColumnInfo(name = "destination") val destination: String?,
)

@Dao
interface MessageDao {
    @Query("SELECT * FROM message_table WHERE conversation_id = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Int): Flow<List<ConversationMessage>>
    
    @Insert
    fun insertMessage(message: ConversationMessage): Long
}

@Entity(tableName = "message_table")
data class ConversationMessage(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "message_id") val messageId: Int = 0,
    @ColumnInfo(name = "conversation_id") val conversationId: Long,
    @ColumnInfo(name = "timestamp") val time: String?,
    @ColumnInfo(name = "is_user") val isFromUser: Boolean,
    @ColumnInfo(name = "text") val text: String?,
    )
