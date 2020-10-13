package `in`.iot.lab.bitscan.ui

import `in`.iot.lab.bitscan.R
import `in`.iot.lab.bitscan.entities.Page
import `in`.iot.lab.bitscan.ui.recyclerView.RecyclerView
import `in`.iot.lab.bitscan.util.Convertors
import android.Manifest
import android.R.array
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class CameraActivity : AppCompatActivity(){

    var cam: Camera? = null
    var preview : Preview? = null
    var capturedImage: ImageCapture? = null
    var camSelector: CameraSelector? = null
    var lensFacing= CameraSelector.LENS_FACING_BACK
    lateinit var photoadrs: File
    lateinit var pageMap : HashMap<Int, Page>
    var index: Int = 0
    private lateinit var sharedPreference : SharedPreferences

    @SuppressLint("SimpleDateFormat", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        //Get current date
        val date = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val formattedDate = sdf.format(date)

        sharedPreference =  getSharedPreferences("BITSCAN_DATA", Context.MODE_PRIVATE)
        getDataFromSharedPreferences()
        checkForCameraPermissions()

        camera_click.setOnClickListener {
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
            photoadrs = File(
                externalMediaDirs.firstOrNull(),
                "BitScan ${System.currentTimeMillis()}.bmp"
            )
            val outputImage= ImageCapture.OutputFileOptions.Builder(photoadrs).build()
            capturedImage ?.takePicture(outputImage, ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        pageMap.put(index, Page(formattedDate, photoadrs.toString()))
                        index++
                        //Log.i("Content-Bitscan", pageMap[0].toString());
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(this@CameraActivity, exception.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                })
        }

        var flash = 0;
        flash_btn.setOnClickListener {
            when(flash){
                0 -> {
                    flash_btn.setImageResource(R.drawable.ic_flash_on_white_18dp)
                    cam?.cameraControl?.enableTorch(true)
                    flash = 1
                }
                1 -> {
                    flash_btn.setImageResource(R.drawable.ic_flash_off_white_18dp)
                    cam?.cameraControl?.enableTorch(false)
                    flash = 0
                }
            }
        }

        gallery_btn2.setOnClickListener {
            val reviewIntent = Intent(this, RecyclerView::class.java)
            val data: String = Convertors.mapToString(pageMap)
            useSharedPreference(data)
            startActivity(reviewIntent)
        }

        menu.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
    }

    private fun checkForCameraPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)
        }
    }

    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
            preview = Preview.Builder().build()
            camSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            preview?.setSurfaceProvider(camView.createSurfaceProvider())
            capturedImage = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
            cam = cameraProvider.bindToLifecycle(this, camSelector!!, preview, capturedImage)

        }, ContextCompat.getMainExecutor(this))

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkForCameraPermissions()
    }

    private fun useSharedPreference(data: String){
        val editor = sharedPreference.edit()
        editor.putString("data", data)
        editor.apply()
    }

    private fun getDataFromSharedPreferences(){
        val data: String? = sharedPreference.getString("data", null)
        if(data!=null) {
            pageMap = Convertors.stringToMap(data) as HashMap<Int, Page>
            index = pageMap.size
        }
        else {
            pageMap = HashMap<Int, Page>()
            index = 0
        }
    }
}