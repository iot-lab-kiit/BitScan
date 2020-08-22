package `in`.iot.lab.bitscan.entities

import android.icu.text.CaseMap
import androidx.room.*
import java.util.*

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    var note_id: Int = 0,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "dateModified") var dateModified: String?,
    @ColumnInfo(name = "onCloud") var onCloud: Boolean,
    @ColumnInfo(name="pageData") var pageData: String
)
//
//data class NoteWithPage(
//    @Embedded val note: Note,
//    @Relation(
//        parentColumn = "note_id",
//        entityColumn = "pageNumber"
//    )
//    val page: List<Page>
//)