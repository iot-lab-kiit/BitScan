package `in`.iot.lab.bitscan.ui

import `in`.iot.lab.bitscan.R
import `in`.iot.lab.bitscan.data.NotesDatabase
import `in`.iot.lab.bitscan.entities.Note
import `in`.iot.lab.bitscan.entities.Page
import `in`.iot.lab.bitscan.ui.recyclerView.RecyclerView
import `in`.iot.lab.bitscan.util.Convertors
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class CameraActivity : AppCompatActivity(){

    private lateinit var db: NotesDatabase
    var cam: Camera? = null
    var preview : Preview? = null
    var capturedImage: ImageCapture? = null
    var camSelector: CameraSelector? = null
    var lensFacing= CameraSelector.LENS_FACING_BACK
    lateinit var photoadrs: File
    var noteID: Long = -1
    var formattedDate = "Date Unknown"
    private lateinit var pageList: MutableList<Page>
    lateinit var currentNote: Note

    @SuppressLint("SimpleDateFormat", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        db = NotesDatabase.getInstance(applicationContext)
        checkForCameraPermissions()

        //Get current date
        val date = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        formattedDate = sdf.format(date)

        //Check if any ID has been passed through intent
        noteID = intent.getLongExtra("noteid",-1)

        if(noteID == -1L){
            createNewNote()
        }
        else{
            retrieveNoteByID(noteID)
        }

        //region FLASH
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
        //endregion

        //region Button Functions
        camera_click.setOnClickListener {
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
            photoadrs = File(
                externalMediaDirs.firstOrNull(),
                "BitScan_temp.bmp"
            )
            val outputImage= ImageCapture.OutputFileOptions.Builder(photoadrs).build()
            capturedImage ?.takePicture(outputImage, ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val bitmap = Convertors.fileToBitmap(photoadrs.path)
                        var byteArray: ByteArray? = null
                        if(bitmap!=null) {
                            byteArray = Convertors.toByteArray(bitmap)
                        }
                        if(byteArray != null) {
                            val page = Page(pageNoteID = noteID,data = byteArray)
                            pageList.add(page)

                            if(getNumPages() == 1L){
                                currentNote.thumbnail = byteArray
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(this@CameraActivity, exception.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                })
        }

        gallery_btn2.setOnClickListener {
            if(pageList.isEmpty()){
                Toast.makeText(this@CameraActivity, "No Images Clicked", Toast.LENGTH_SHORT).show()
            }
            else {
                val reviewIntent = Intent(this, RecyclerView::class.java)
                reviewIntent.putExtra("noteid", noteID)
                modifyNote()
                startActivity(reviewIntent)
            }
        }

        menu.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            handleEmptyNote()
        }
        //endregion
    }

    //region Camera functions
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
    //endregion

    //region Database Functions
    private fun createNewNote()= runBlocking{
        launch {
            val note = Note(
                title = "NULL",
                dateModified = formattedDate,
                onCloud = false,
                pdfPath = "Unavailable",
                thumbnail = null,
                numPages = 0
            )
            noteID = db.noteDao().insertNote(note)
            retrieveNoteByID(noteID)
        }
    }

    private fun modifyNote()= runBlocking{
        launch {
            currentNote.title = "New Document $noteID"
            currentNote.numPages = pageList.size
            db.noteDao().insertNote(currentNote)
            db.noteDao().insertPages(pageList)
            pageList.removeAll(pageList)
            goToDashboard()
        }
    }

    private fun retrieveNoteByID(id:Long)= runBlocking{
        launch {
            pageList = ArrayList()
            val list = db.noteDao().getNote(id)
            currentNote = list[0].note
            pageList = list[0].pages as MutableList<Page>
        }
    }

    private fun deleteNote(note: Note)= runBlocking{
        launch {
            db.noteDao().deleteNote(note)
            goToDashboard()
        }
    }
    //endregion

    fun getNumPages(): Long{
        return pageList.size.toLong()
    }

    private fun handleEmptyNote(){
        if (currentNote.numPages == 0 && pageList.isEmpty()){
            deleteNote(currentNote)
        }
        else{
            modifyNote()
        }
    }

    private fun goToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
       handleEmptyNote()
    }
}