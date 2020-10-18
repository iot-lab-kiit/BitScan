package `in`.iot.lab.bitscan.ui

import `in`.iot.lab.bitscan.R
import `in`.iot.lab.bitscan.data.NotesDatabase
import `in`.iot.lab.bitscan.entities.Page
import `in`.iot.lab.bitscan.ui.recyclerView.RecyclerView
import `in`.iot.lab.bitscan.util.Convertors
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_page_review.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream


class PageReviewActivity : AppCompatActivity() {

    private lateinit var db: NotesDatabase
    lateinit var uri: Uri
    lateinit var modifiedUri: Uri
    var noteID: Long = -1
    var pageID: Long = -1
    lateinit  var page: Page
    var backAllowed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_review)
        db = NotesDatabase.getInstance(applicationContext)

        noteID = intent.getLongExtra("noteid", -1)
        pageID = intent.getLongExtra("pageid", -1)
        retrievePage(noteID, pageID)

        crop_img.setOnClickListener{
            launchImageCrop(uri)
        }

        continue_btn.setOnClickListener {
            modifyPage(modifiedUri);
        }
        
        discard_btn.setOnClickListener { 
            goToAllPageView()
        }
    }

    private fun launchImageCrop(uri: Uri){
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)
    }

    private fun retrievePage(id: Long, pageid: Long)= runBlocking{
        launch {
            page = db.noteDao().getPage(id,pageid)[0]
            val temp = Convertors.toBitmap(page.data)
            if(temp!=null) {
                convertBitmapToUri(temp)
            }
        }
    }

    private fun goToAllPageView(){
        val i = Intent(this, RecyclerView::class.java)
        i.putExtra("noteid", noteID)
        startActivity(i)
        finish()
    }

    private fun modifyPage(uri: Uri)= runBlocking {
        launch {
            val bmp = uri.path?.let { Convertors.fileToBitmap(it) }
            val arr = bmp?.let { Convertors.toByteArray(it) }
            if (arr != null) {
                db.noteDao().modifyPage(arr,noteId = noteID,pageID = pageID)
            }

            val list = db.noteDao().getAllPages(noteID)
            val temp = db.noteDao().getNote(noteID)
            temp[0].note.thumbnail = list[0].data
            db.noteDao().insertNote(temp[0].note)

            goToAllPageView()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    result.uri?.let {
                        setImage(it)
                        modifiedUri = it
                    }
                } else {
                    Toast.makeText(this, "Unable to crop Image!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setImage(uri: Uri){
        Glide.with(this)
            .load(uri)
            .fitCenter()
            .apply(RequestOptions.skipMemoryCacheOf(true))
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
            .into(showPageForReview)
    }


    private fun convertBitmapToUri(bmp: Bitmap) {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val filePath: String = getExternalFilesDir(null)?.absolutePath.toString()
        val file = File(filePath, "temp.bmp")

        class ConvertImageTask : AsyncTask<Void?, Void?, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                try {
                    FileOutputStream(file).use { out ->
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                uri = Uri.fromFile(file)
                modifiedUri = uri
                setImage(uri)
                page_review_bottom_menu.visibility = View.VISIBLE
                page_review_progress_circular.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                backAllowed = true
            }
        }
        ConvertImageTask().execute()
    }

    override fun onBackPressed() {
        if(backAllowed) {
            super.onBackPressed()
        }
        else {
            Toast.makeText(applicationContext,"Image Loading. Please Wait!",Toast.LENGTH_SHORT).show()
        }
    }
}