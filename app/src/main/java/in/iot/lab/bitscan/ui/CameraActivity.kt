package `in`.iot.lab.bitscan.ui

import `in`.iot.lab.bitscan.R
import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File


class CameraActivity : AppCompatActivity() {

    var cam: Camera? = null
    var preview : Preview? = null
    var capturedImage: ImageCapture? = null
    var camSelector: CameraSelector? = null
    var lensFacing= CameraSelector.LENS_FACING_BACK

    lateinit var photoadrs: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)



        checkForCameraPermissions()

        camera_click.setOnClickListener {
            Toast.makeText(this, "wasdtgbn", Toast.LENGTH_SHORT).show()
            photoadrs = File(externalMediaDirs.firstOrNull(), "BitScan ${System.currentTimeMillis()}.bmp" )
            val outputImage= ImageCapture.OutputFileOptions.Builder(photoadrs).build()
            capturedImage ?.takePicture(outputImage, ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Toast.makeText(this@CameraActivity, "Imager Saved at $photoadrs", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(this@CameraActivity, exception.message, Toast.LENGTH_SHORT).show()
                    }
                })
        }


        var flash= 0
        flash_btn.setOnClickListener {
            when(flash){
                0-> {
                    flash_btn.setImageResource(R.drawable.ic_flash_on_white_18dp)
                    flash = 1
                }
                1-> {
                    flash_btn.setImageResource(R.drawable.ic_flash_off_white_18dp)
                    flash = -1
                }
                -1-> {
                    flash_btn.setImageResource(R.drawable.ic_flash_auto_white_18dp)
                    flash = 0
                }
            }
        }
//this was made to check the intent, can be removed
//        camera_click.setOnClickListener {
//            startActivity(Intent(this, PageReviewActivity::class.java))
//        }
    }

    private fun checkForCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PermissionChecker.PERMISSION_GRANTED)
        {
            startCamera()
        }
        else{

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)
        }
    }

    private fun startCamera() {

        var cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
            preview= Preview.Builder().build()
            camSelector= CameraSelector.Builder().requireLensFacing(lensFacing).build()
            preview?.setSurfaceProvider(camView.createSurfaceProvider())
            capturedImage = ImageCapture.Builder().build()
            cam= cameraProvider.bindToLifecycle(this, camSelector!!, preview, capturedImage)

        }, ContextCompat.getMainExecutor(this))
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        checkForCameraPermissions()
    }



}