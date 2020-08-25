package `in`.iot.lab.bitscan.interfaces

import `in`.iot.lab.bitscan.enums.ScanHint
import android.graphics.Bitmap

/**
 * Interface between activity and surface view
 */
interface IScanner {
    fun displayHint(scanHint: ScanHint?)
    fun onPictureClicked(bitmap: Bitmap?)
}