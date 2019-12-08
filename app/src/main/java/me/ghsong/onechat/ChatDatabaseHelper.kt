package me.ghsong.onechat

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ChatDatabaseHelper(context: Context): SQLiteOpenHelper(context, DB_FILE, null, DB_VERSION) {

    /**
     * 최초 한번 싷앵
     */
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TABLE_CHAT (_id INTEGER PRIMARY KEY AUTOINCREMENT, message TEXT NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // margraion/
    }

    companion object {
        const val DB_FILE = "chat_db"
        const val DB_VERSION = 1
        const val TABLE_CHAT = "CHAT_TABLE"
    }
}