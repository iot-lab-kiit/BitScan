package `in`.iot.lab.bitscan.entities

import android.icu.text.CaseMap
import androidx.room.*
import java.util.*

@Entity(tableName = "notes")
class Note(
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "dateModified") var dateModified: String?,
    @ColumnInfo(name = "onCloud") var onCloud: Boolean,
    @ColumnInfo(name="thumbnailPath") var thumbnail: String,
    @ColumnInfo(name="pageData") var pageData: String,
    @ColumnInfo(name="pdfPath") var pdfPath: String
) {
    @PrimaryKey(autoGenerate = true)
    var noteID: Int = 0
}
