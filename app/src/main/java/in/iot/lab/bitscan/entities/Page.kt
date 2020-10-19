package `in`.iot.lab.bitscan.entities

import androidx.room.*


@Entity(tableName = "pages")
class Page(
    val pageNoteID: Long,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var data: ByteArray? = null
){
    @PrimaryKey(autoGenerate = true)
    var pageID: Long = 0

    override fun toString(): String {
        var x : String = "No data"
        if(data!=null) {
            x = "OK"
        }
        return "$pageNoteID $pageID $x"
    }
}