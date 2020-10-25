package `in`.iot.lab.bitscan.util

import `in`.iot.lab.bitscan.entities.Page
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore.Images
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream


class Convertors {
    companion object {
        fun stringToMap(value: String): Map<Int, Page> {
            return Gson().fromJson(value, object : TypeToken<Map<Int, Page>>() {}.type)
        }

        fun mapToString(value: Map<Int, Page>?): String {
            return if (value == null) ""
            else {
                return Gson().toJson(value)
            }
        }

        fun toByteArray(bitmap: Bitmap):ByteArray{
            ByteArrayOutputStream().apply {
                bitmap.compress(CompressFormat.JPEG, 60, this)
                return toByteArray()
            }
        }
        
        fun toBitmap(byteArray: ByteArray?):Bitmap?{
            return if (byteArray != null) {
                try {
                    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                } catch (e: Exception){
                    e.printStackTrace()
                    null
                }
            } else {
                null
            }
        }

        fun fileToBitmap(path: String):Bitmap?{
            return try {
                BitmapFactory.decodeFile(path)
            }
            catch (e: Exception){
                e.printStackTrace()
                null
            }
        }
    }
}