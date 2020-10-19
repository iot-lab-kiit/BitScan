package `in`.iot.lab.bitscan.entities

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithPages(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "noteID",
        entityColumn = "pageNoteID"
    )
    val pages: List<Page>
)
