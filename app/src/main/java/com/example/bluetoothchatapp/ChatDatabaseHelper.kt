package com.example.bluetoothchatapp

import ChatMessage
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class ChatDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "ChatMessages.db"

        // Define table and column names
        private const val TABLE_NAME = "chat_messages"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_TEXT = "text"
        private const val COLUMN_IS_SENT = "is_sent"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSQL = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TEXT TEXT,
                $COLUMN_IS_SENT INTEGER
            );
        """
        db.execSQL(createTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // You can handle database schema upgrades here if needed
    }

    fun insertChatMessage(message: ChatMessage): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TEXT, message.text)
            put(COLUMN_IS_SENT, if (message.isSent) 1 else 0)
        }
        val id = db.insert(TABLE_NAME, null, values)
        db.close()

        Log.d("DatabaseInsert", "Inserted message: ${message.text}, isSent: ${message.isSent}")

        return id
    }


    fun getAllChatMessages(): List<ChatMessage> {
        val chatMessages = mutableListOf<ChatMessage>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            val idColumnIndex = cursor.getColumnIndex(COLUMN_ID)
            val textColumnIndex = cursor.getColumnIndex(COLUMN_TEXT)
            val isSentColumnIndex = cursor.getColumnIndex(COLUMN_IS_SENT)

            if (idColumnIndex != -1 && textColumnIndex != -1 && isSentColumnIndex != -1) {
                val id = cursor.getLong(idColumnIndex)
                val text = cursor.getString(textColumnIndex)
                val isSent = cursor.getInt(isSentColumnIndex) == 1
                chatMessages.add(ChatMessage(id, text, isSent))

                Log.d("DatabaseRetrieve", "Retrieved message: $text, isSent: $isSent")
            }
        }
        cursor.close()
        db.close()
        return chatMessages
    }
}
