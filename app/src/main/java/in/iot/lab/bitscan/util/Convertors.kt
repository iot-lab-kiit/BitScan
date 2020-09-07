package `in`.iot.lab.bitscan.util

import `in`.iot.lab.bitscan.entities.Page
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
    }
}