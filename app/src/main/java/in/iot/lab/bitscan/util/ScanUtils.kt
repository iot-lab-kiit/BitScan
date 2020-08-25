package `in`.iot.lab.bitscan.util

import `in`.iot.lab.bitscan.constants.ScanConstants
import `in`.iot.lab.bitscan.view.Quadrilateral
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Surface
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.utils.Converters
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * This class provides utilities for camera.
 */
object ScanUtils {
    private val TAG = ScanUtils::class.java.simpleName
    @JvmStatic
    fun compareFloats(left: Double, right: Double): Boolean {
        val epsilon = 0.00000001
        return Math.abs(left - right) < epsilon
    }

    @JvmStatic
    fun determinePictureSize(
        camera: Camera?,
        previewSize: Camera.Size
    ): Camera.Size? {
        if (camera == null) return null
        val cameraParams = camera.parameters
        val pictureSizeList =
            cameraParams.supportedPictureSizes
        Collections.sort(
            pictureSizeList
        ) { size1, size2 ->
            val h1 =
                Math.sqrt(size1.width * size1.width + size1.height * size1.height.toDouble())
            val h2 =
                Math.sqrt(size2.width * size2.width + size2.height * size2.height.toDouble())
            h2.compareTo(h1)
        }
        var retSize: Camera.Size? = null

        // if the preview size is not supported as a picture size
        val reqRatio = previewSize.width.toFloat() / previewSize.height
        var curRatio: Float
        var deltaRatio: Float
        var deltaRatioMin = Float.MAX_VALUE
        for (size in pictureSizeList) {
            curRatio = size.width.toFloat() / size.height
            deltaRatio = Math.abs(reqRatio - curRatio)
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio
                retSize = size
            }
            if (compareFloats(deltaRatio.toDouble(), 0.0)) {
                break
            }
        }
        return retSize
    }

    fun getOptimalPreviewSize(
        camera: Camera?,
        w: Int,
        h: Int
    ): Camera.Size? {
        if (camera == null) return null
        val targetRatio = h.toDouble() / w
        val cameraParams = camera.parameters
        val previewSizeList =
            cameraParams.supportedPreviewSizes
        Collections.sort(
            previewSizeList,
            Comparator { size1, size2 ->
                val ratio1 = size1.width.toDouble() / size1.height
                val ratio2 = size2.width.toDouble() / size2.height
                val ratioDiff1 = Math.abs(ratio1 - targetRatio)
                val ratioDiff2 = Math.abs(ratio2 - targetRatio)
                if (compareFloats(ratioDiff1, ratioDiff2)) {
                    val h1 =
                        Math.sqrt(size1.width * size1.width + size1.height * size1.height.toDouble())
                    val h2 =
                        Math.sqrt(size2.width * size2.width + size2.height * size2.height.toDouble())
                    return@Comparator h2.compareTo(h1)
                }
                ratioDiff1.compareTo(ratioDiff2)
            })
        return previewSizeList[0]
    }

    fun getDisplayOrientation(activity: Activity, cameraId: Int): Int {
        val info = CameraInfo()
        val rotation = activity.windowManager.defaultDisplay.rotation
        var degrees = 0
        val dm = DisplayMetrics()
        Camera.getCameraInfo(cameraId, info)
        activity.windowManager.defaultDisplay.getMetrics(dm)
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var displayOrientation: Int
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            displayOrientation = (info.orientation + degrees) % 360
            displayOrientation = (360 - displayOrientation) % 360
        } else {
            displayOrientation = (info.orientation - degrees + 360) % 360
        }
        return displayOrientation
    }

    fun getOptimalPictureSize(
        camera: Camera?,
        width: Int,
        height: Int,
        previewSize: Camera.Size
    ): Camera.Size? {
        if (camera == null) return null
        val cameraParams = camera.parameters
        val supportedSizes =
            cameraParams.supportedPictureSizes
        val size = camera.Size(width, height)

        // convert to landscape if necessary
        if (size.width < size.height) {
            val temp = size.width
            size.width = size.height
            size.height = temp
        }
        val requestedSize = camera.Size(size.width, size.height)
        var previewAspectRatio =
            previewSize.width.toDouble() / previewSize.height.toDouble()
        if (previewAspectRatio < 1.0) {
            // reset ratio to landscape
            previewAspectRatio = 1.0 / previewAspectRatio
        }
        Log.d(TAG, "CameraPreview previewAspectRatio $previewAspectRatio")
        val aspectTolerance = 0.1
        var bestDifference = Double.MAX_VALUE
        for (i in supportedSizes.indices) {
            val supportedSize = supportedSizes[i]

            // Perfect match
            if (supportedSize == requestedSize) {
                Log.d(
                    TAG,
                    "CameraPreview optimalPictureSize " + supportedSize.width + 'x' + supportedSize.height
                )
                return supportedSize
            }
            val difference =
                Math.abs(previewAspectRatio - supportedSize.width.toDouble() / supportedSize.height.toDouble())
            if (difference < bestDifference - aspectTolerance) {
                // better aspectRatio found
                if (width != 0 && height != 0 || supportedSize.width * supportedSize.height < 2048 * 1024) {
                    size.width = supportedSize.width
                    size.height = supportedSize.height
                    bestDifference = difference
                }
            } else if (difference < bestDifference + aspectTolerance) {
                // same aspectRatio found (within tolerance)
                if (width == 0 || height == 0) {
                    // set highest supported resolution below 2 Megapixel
                    if (size.width < supportedSize.width && supportedSize.width * supportedSize.height < 2048 * 1024) {
                        size.width = supportedSize.width
                        size.height = supportedSize.height
                    }
                } else {
                    // check if this pictureSize closer to requested width and height
                    if (Math.abs(width * height - supportedSize.width * supportedSize.height) < Math.abs(
                            width * height - size.width * size.height
                        )
                    ) {
                        size.width = supportedSize.width
                        size.height = supportedSize.height
                    }
                }
            }
        }
        Log.d(
            TAG,
            "CameraPreview optimalPictureSize " + size.width + 'x' + size.height
        )
        return size
    }

    fun getOptimalPreviewSize(
        displayOrientation: Int,
        sizes: List<Camera.Size>?,
        w: Int,
        h: Int
    ): Camera.Size? {
        val ASPECT_TOLERANCE = 0.1
        var targetRatio = w.toDouble() / h
        if (displayOrientation == 90 || displayOrientation == 270) {
            targetRatio = h.toDouble() / w
        }
        if (sizes == null) {
            return null
        }
        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE

        // Try to find an size match aspect ratio and size
        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - h).toDouble()
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE
            for (size in sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - h).toDouble()
                }
            }
        }
        Log.d(
            "optimal preview size",
            "w: " + optimalSize!!.width + " h: " + optimalSize.height
        )
        return optimalSize
    }

    @JvmStatic
    fun configureCameraAngle(activity: Activity): Int {
        val angle: Int
        val display = activity.windowManager.defaultDisplay
        angle = when (display.rotation) {
            Surface.ROTATION_0 -> 90 // This is camera orientation
            Surface.ROTATION_90 -> 0
            Surface.ROTATION_180 -> 270
            Surface.ROTATION_270 -> 180
            else -> 90
        }
        return angle
    }

    fun getMaxCosine(
        maxCosine: Double,
        approxPoints: Array<Point>
    ): Double {
        var maxCosine = maxCosine
        Log.i(TAG, "ANGLES ARE:")
        for (i in 2..4) {
            val cosine = Math.abs(
                angle(
                    approxPoints[i % 4],
                    approxPoints[i - 2],
                    approxPoints[i - 1]
                )
            )
            Log.i(TAG, cosine.toString())
            maxCosine = Math.max(cosine, maxCosine)
        }
        return maxCosine
    }

    private fun angle(
        p1: Point,
        p2: Point,
        p0: Point
    ): Double {
        val dx1 = p1.x - p0.x
        val dy1 = p1.y - p0.y
        val dx2 = p2.x - p0.x
        val dy2 = p2.y - p0.y
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10)
    }

    private fun sortPoints(src: Array<Point>): Array<Point?> {
        val srcPoints =
            ArrayList(Arrays.asList(*src))
        val result =
            arrayOf<Point?>(null, null, null, null)
        val sumComparator =
            Comparator<Point> { lhs, rhs ->
                java.lang.Double.valueOf(lhs.y + lhs.x).compareTo(rhs.y + rhs.x)
            }
        val diffComparator =
            Comparator<Point> { lhs, rhs ->
                java.lang.Double.valueOf(lhs.y - lhs.x).compareTo(rhs.y - rhs.x)
            }

        // top-left corner = minimal sum
        result[0] = Collections.min(srcPoints, sumComparator)
        // bottom-right corner = maximal sum
        result[2] = Collections.max(srcPoints, sumComparator)
        // top-right corner = minimal difference
        result[1] = Collections.min(srcPoints, diffComparator)
        // bottom-left corner = maximal difference
        result[3] = Collections.max(srcPoints, diffComparator)
        return result
    }

    private val morph_kernel = Mat(
        Size(ScanConstants.KSIZE_CLOSE.toDouble(), ScanConstants.KSIZE_CLOSE.toDouble()),
        CvType.CV_8UC1,
        Scalar(255.0)
    )

    @JvmStatic
    fun detectLargestQuadrilateral(originalMat: Mat): Quadrilateral? {
        Imgproc.cvtColor(originalMat, originalMat, Imgproc.COLOR_BGR2GRAY, 4)

        // Just OTSU/Binary thresholding is not enough.
        //Imgproc.threshold(mGrayMat, mGrayMat, 150, 255, THRESH_BINARY + THRESH_OTSU);

        /*
        *  1. We shall first blur and normalize the image for uniformity,
        *  2. Truncate light-gray to white and normalize,
        *  3. Apply canny edge detection,
        *  4. Cutoff weak edges,
        *  5. Apply closing(morphology), then proceed to finding contours.
        */

        // step 1.
        Imgproc.blur(
            originalMat,
            originalMat,
            Size(ScanConstants.KSIZE_BLUR.toDouble(), ScanConstants.KSIZE_BLUR.toDouble())
        )
        Core.normalize(originalMat, originalMat, 0.0, 255.0, Core.NORM_MINMAX)
        // step 2.
        // As most papers are bright in color, we can use truncation to make it uniformly bright.
        Imgproc.threshold(
            originalMat,
            originalMat,
            ScanConstants.TRUNC_THRESH.toDouble(),
            255.0,
            Imgproc.THRESH_TRUNC
        )
        Core.normalize(originalMat, originalMat, 0.0, 255.0, Core.NORM_MINMAX)
        // step 3.
        // After above preprocessing, canny edge detection can now work much better.
        Imgproc.Canny(
            originalMat,
            originalMat,
            ScanConstants.CANNY_THRESH_U.toDouble(),
            ScanConstants.CANNY_THRESH_L.toDouble()
        )
        // step 4.
        // Cutoff the remaining weak edges
        Imgproc.threshold(
            originalMat,
            originalMat,
            ScanConstants.CUTOFF_THRESH.toDouble(),
            255.0,
            Imgproc.THRESH_TOZERO
        )
        // step 5.
        // Closing - closes small gaps. Completes the edges on canny image; AND also reduces stringy lines near edge of paper.
        Imgproc.morphologyEx(
            originalMat,
            originalMat,
            Imgproc.MORPH_CLOSE,
            morph_kernel,
            Point(-1.0, -1.0),
            1
        )

        // Get only the 10 largest contours (each approximated to their convex hulls)
        val largestContour =
            findLargestContours(originalMat, 10)
        if (null != largestContour) {
            val mLargestRect: Quadrilateral? = findQuadrilateral(largestContour)
            if (mLargestRect != null) return mLargestRect
        }
        return null
    }

    private fun hull2Points(hull: MatOfInt, contour: MatOfPoint): MatOfPoint {
        val indexes = hull.toList()
        val points: MutableList<Point> =
            ArrayList()
        val ctrList = contour.toList()
        for (index in indexes) {
            points.add(ctrList[index])
        }
        val point = MatOfPoint()
        point.fromList(points)
        return point
    }

    private fun findLargestContours(
        inputMat: Mat,
        NUM_TOP_CONTOURS: Int
    ): List<MatOfPoint>? {
        val mHierarchy = Mat()
        val mContourList: List<MatOfPoint> = ArrayList()
        //finding contours - as we are sorting by area anyway, we can use RETR_LIST - faster than RETR_EXTERNAL.
        Imgproc.findContours(
            inputMat,
            mContourList,
            mHierarchy,
            Imgproc.RETR_LIST,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        // Convert the contours to their Convex Hulls i.e. removes minor nuances in the contour
        val mHullList: MutableList<MatOfPoint> =
            ArrayList()
        val tempHullIndices = MatOfInt()
        for (i in mContourList.indices) {
            Imgproc.convexHull(mContourList[i], tempHullIndices)
            mHullList.add(hull2Points(tempHullIndices, mContourList[i]))
        }
        // Release mContourList as its job is done
        for (c in mContourList) c.release()
        tempHullIndices.release()
        mHierarchy.release()
        if (mHullList.size != 0) {
            Collections.sort(mHullList) { lhs, rhs ->
                java.lang.Double.compare(
                    Imgproc.contourArea(rhs),
                    Imgproc.contourArea(lhs)
                )
            }
            return mHullList.subList(0, Math.min(mHullList.size, NUM_TOP_CONTOURS))
        }
        return null
    }

    private fun findQuadrilateral(mContourList: List<MatOfPoint>): Quadrilateral? {
        for (c in mContourList) {
            val c2f = MatOfPoint2f(*c.toArray())
            val peri = Imgproc.arcLength(c2f, true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true)
            val points = approx.toArray()
            // select biggest 4 angles polygon
            if (approx.rows() == 4) {
                val foundPoints = sortPoints(points)
                return Quadrilateral(approx, foundPoints)
            }
        }
        return null
    }

    fun enhanceReceipt(
        image: Bitmap,
        topLeft: Point,
        topRight: Point,
        bottomLeft: Point,
        bottomRight: Point
    ): Bitmap {
        var resultWidth = (topRight.x - topLeft.x).toInt()
        val bottomWidth = (bottomRight.x - bottomLeft.x).toInt()
        if (bottomWidth > resultWidth) resultWidth = bottomWidth
        var resultHeight = (bottomLeft.y - topLeft.y).toInt()
        val bottomHeight = (bottomRight.y - topRight.y).toInt()
        if (bottomHeight > resultHeight) resultHeight = bottomHeight
        val inputMat = Mat(image.height, image.height, CvType.CV_8UC1)
        Utils.bitmapToMat(image, inputMat)
        val outputMat = Mat(resultWidth, resultHeight, CvType.CV_8UC1)
        val source: MutableList<Point> =
            ArrayList()
        source.add(topLeft)
        source.add(topRight)
        source.add(bottomLeft)
        source.add(bottomRight)
        val startM = Converters.vector_Point2f_to_Mat(source)
        val ocvPOut1 = Point(0.0, 0.0)
        val ocvPOut2 = Point(resultWidth.toDouble(), 0.0)
        val ocvPOut3 = Point(0.0, resultHeight.toDouble())
        val ocvPOut4 = Point(resultWidth.toDouble(), resultHeight.toDouble())
        val dest: MutableList<Point> =
            ArrayList()
        dest.add(ocvPOut1)
        dest.add(ocvPOut2)
        dest.add(ocvPOut3)
        dest.add(ocvPOut4)
        val endM = Converters.vector_Point2f_to_Mat(dest)
        val perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM)
        Imgproc.warpPerspective(
            inputMat,
            outputMat,
            perspectiveTransform,
            Size(resultWidth.toDouble(), resultHeight.toDouble())
        )
        val output = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(outputMat, output)
        return output
    }

    fun saveToInternalMemory(
        bitmap: Bitmap,
        mFileDirectory: String,
        mFileName: String?,
        mContext: Context,
        mQuality: Int
    ): Array<String?> {
        val mReturnParams = arrayOfNulls<String>(2)
        val mDirectory =
            getBaseDirectoryFromPathString(mFileDirectory, mContext)
        val mPath = File(mDirectory, mFileName)
        try {
            val mFileOutputStream = FileOutputStream(mPath)
            //Compress method used on the Bitmap object to write  image to output stream
            bitmap.compress(Bitmap.CompressFormat.JPEG, mQuality, mFileOutputStream)
            mFileOutputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
        mReturnParams[0] = mDirectory.absolutePath
        mReturnParams[1] = mFileName
        return mReturnParams
    }

    private fun getBaseDirectoryFromPathString(
        mPath: String,
        mContext: Context
    ): File {
        val mContextWrapper = ContextWrapper(mContext)
        return mContextWrapper.getDir(mPath, Context.MODE_PRIVATE)
    }

    fun decodeBitmapFromFile(path: String?, imageName: String?): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        return BitmapFactory.decodeFile(
            File(path, imageName).absolutePath,
            options
        )
    }

    /*
     * This method converts the dp value to px
     * @param context context
     * @param dp value in dp
     * @return px value
     */
    @JvmStatic
    fun dp2px(context: Context, dp: Float): Int {
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
        return Math.round(px)
    }

    @JvmStatic
    fun decodeBitmapFromByteArray(data: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap {
        // Raw height and width of image
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(data, 0, data.size, options)

        // Calculate inSampleSize
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        options.inSampleSize = inSampleSize

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(data, 0, data.size, options)
    }

    @Deprecated("")
    fun loadEfficientBitmap(data: ByteArray, width: Int, height: Int): Bitmap {
        val bmp: Bitmap

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(data, 0, data.size, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width, height)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        bmp = BitmapFactory.decodeByteArray(data, 0, data.size, options)
        return bmp
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int
    ): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight
                && halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var image = image
        return if (maxHeight > 0 && maxWidth > 0) {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > 1) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
            image
        } else {
            image
        }
    }

    fun resizeToScreenContentSize(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }

    fun getPolygonDefaultPoints(bitmap: Bitmap): ArrayList<PointF> {
        val points: ArrayList<PointF>
        points = ArrayList()
        points.add(
            PointF(
                bitmap.width * 0.14f,
                bitmap.height.toFloat() * 0.13f
            )
        )
        points.add(
            PointF(
                bitmap.width * 0.84f,
                bitmap.height.toFloat() * 0.13f
            )
        )
        points.add(
            PointF(
                bitmap.width * 0.14f,
                bitmap.height.toFloat() * 0.83f
            )
        )
        points.add(
            PointF(
                bitmap.width * 0.84f,
                bitmap.height.toFloat() * 0.83f
            )
        )
        return points
    }

    fun isScanPointsValid(points: Map<Int, PointF>): Boolean {
        return points.size == 4
    }
}