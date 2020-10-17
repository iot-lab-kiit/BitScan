package `in`.iot.lab.bitscan.entities

import androidx.room.*

@Entity(tableName = "notes")
class Note(
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "dateModified") var dateModified: String?,
    @ColumnInfo(name = "onCloud") var onCloud: Boolean,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB,name="thumbnail")
    var thumbnail: ByteArray? = null,
    @ColumnInfo(name="pdfPath") var pdfPath: String,
    var numPages: Int = 0
) {
    @PrimaryKey(autoGenerate = true)
    var noteID: Long = 0

    override fun toString(): String {
        return "$noteID $title $dateModified $pdfPath"
    }
}
