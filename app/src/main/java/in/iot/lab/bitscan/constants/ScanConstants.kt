package `in`.iot.lab.bitscan.constants

/**
 * This class defines constants
 */
object ScanConstants {
    const val SCANNED_RESULT = "scannedResult"
    const val IMAGE_NAME = "image"
    const val IMAGE_DIR = "imageDir"
    const val HIGHER_SAMPLING_THRESHOLD = 2200
    @JvmField
    var KSIZE_BLUR = 3
    @JvmField
    var KSIZE_CLOSE = 10
    const val CANNY_THRESH_L = 85
    const val CANNY_THRESH_U = 185
    const val TRUNC_THRESH = 150
    const val CUTOFF_THRESH = 155
}