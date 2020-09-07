package `in`.iot.lab.bitscan.dao

import `in`.iot.lab.bitscan.entities.Note
import `in`.iot.lab.bitscan.entities.Page
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY note_id")
    fun getAllNotes(): LiveData<List<Note>>

    @Transaction
    @Query("SELECT * FROM notes WHERE note_id =:noteId")
    fun getAllPages(noteId: Int): Note

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}