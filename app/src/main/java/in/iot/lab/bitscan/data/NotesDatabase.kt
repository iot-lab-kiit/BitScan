package `in`.iot.lab.bitscan.data

import `in`.iot.lab.bitscan.dao.NoteDao
import `in`.iot.lab.bitscan.entities.Note
import `in`.iot.lab.bitscan.entities.Page
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class, Page::class], version = 1)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao() : NoteDao
    companion object{
        private var INSTANCE: NotesDatabase? = null

        fun getInstance(context: Context): NotesDatabase{
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(
                    context,
                    NotesDatabase::class.java,
                    "notes_db")
                    .build()
            }
            return INSTANCE as NotesDatabase
        }
    }
}