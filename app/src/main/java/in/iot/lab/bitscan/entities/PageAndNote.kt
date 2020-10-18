package `in`.iot.lab.bitscan.entities

import androidx.room.Embedded
import androidx.room.Relation

data class PageAndNote(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "noteID",
        entityColumn = "pageNoteID"
    )
    val page: Page
)