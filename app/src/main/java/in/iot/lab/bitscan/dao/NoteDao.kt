package `in`.iot.lab.bitscan.dao

import `in`.iot.lab.bitscan.entities.Note
import `in`.iot.lab.bitscan.entities.NoteWithPages
import `in`.iot.lab.bitscan.entities.Page
import androidx.lifecycle.LiveData
import androidx.room.*;

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<Page>)

    @Transaction
    @Query("UPDATE pages SET data = :arr WHERE pageNoteID=:noteId AND pageID = :pageID")
    suspend fun modifyPage(arr: ByteArray,noteId: Long,pageID: Long)

    @Transaction
    @Query("SELECT * FROM notes ORDER BY noteID")
    fun getAllNotes(): LiveData<List<NoteWithPages>>

    @Transaction
    @Query("SELECT * FROM notes WHERE noteID =:noteId ORDER BY noteID")
    suspend fun getNote(noteId: Long): List<NoteWithPages>

    @Transaction
    @Query("SELECT * FROM pages WHERE pageNoteID =:noteId ORDER BY pageID")
    suspend fun getAllPages(noteId: Long): List<Page>

    @Transaction
    @Query("SELECT * FROM pages WHERE pageNoteID=:noteId AND pageID = :pageID")
    suspend fun getPage(noteId: Long,pageID: Long):  List<Page>

    @Transaction
    @Query("Delete FROM pages WHERE pageNoteID=:noteId AND pageID = :pageID")
    suspend fun deletePage(noteId: Long,pageID: Long)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}