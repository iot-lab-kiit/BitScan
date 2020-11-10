package `in`.iot.lab.bitscan.ui.recyclerView

import `in`.iot.lab.bitscan.R
import `in`.iot.lab.bitscan.data.NotesDatabase
import `in`.iot.lab.bitscan.entities.Note
import `in`.iot.lab.bitscan.entities.Page
import `in`.iot.lab.bitscan.ui.CameraActivity
import `in`.iot.lab.bitscan.ui.DashboardActivity
import `in`.iot.lab.bitscan.ui.PageReviewActivity
import `in`.iot.lab.bitscan.ui.PdfReviewActivity
import `in`.iot.lab.bitscan.util.Convertors
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.github.dhaval2404.imagepicker.ImagePicker
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.drag.ItemTouchCallback
import com.mikepenz.fastadapter.drag.SimpleDragCallback
import com.mikepenz.fastadapter.drag.SimpleDragCallback.Companion.ALL
import com.mikepenz.fastadapter.select.getSelectExtension
import com.mikepenz.fastadapter.utils.DragDropUtil
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_recycler_view.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.delete_dialog_layout.view.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class RecyclerView : AppCompatActivity() {

    private lateinit var db: NotesDatabase
    lateinit var pageList : MutableList<ListObject>
    var noteID: Long = -1
    var pageID: Long = -1
    lateinit var selectedNote: Note
    lateinit var notePageList: MutableList<Page>
    lateinit var pdfPath: String
    lateinit var itemAdapter: ItemAdapter<ListObject>
    lateinit var fastAdapter: FastAdapter<ListObject>
    var dialogURL : AlertDialog? = null
    var backAllowed = true

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)
        db = NotesDatabase.getInstance(applicationContext)

        //Check if any ID has been passed through intent
        noteID = intent.getLongExtra("noteid", -1)
        notePageList = ArrayList()
        retrieveNoteByID(noteID)

        reorder_add_gallery.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .galleryOnly()
                .galleryMimeTypes(
                    mimeTypes = arrayOf(
                        "image/png",
                        "image/jpg",
                        "image/jpeg"
                    )
                )
                .start(101)
        }

    }

    private fun displayData(){
        if(notePageList.isNotEmpty()) {
            if(selectedNote.title != "NULL"){
                reorder_document_name.setText(selectedNote.title)
            }

            itemAdapter = ItemAdapter<ListObject>()
            //create the managing FastAdapter, by passing in the itemAdapter
            fastAdapter = FastAdapter.with(itemAdapter)

            reorder_recycler_view.apply {
                layoutManager = GridLayoutManager(this@RecyclerView, 2)
                adapter = fastAdapter
            }

            val selectExtension = fastAdapter.getSelectExtension()
            selectExtension.isSelectable = true
            selectExtension.multiSelect = true
            selectExtension.selectOnLongClick = true

            fastAdapter.onClickListener = { v: View?, _: IAdapter<ListObject>, _: ListObject, position: Int ->
                v?.let {
                    val intent = Intent(this, PageReviewActivity::class.java)
                    intent.putExtra("noteid", noteID)
                    intent.putExtra("pageid", notePageList[position].pageID)
                    startActivity(intent)
                }
                false
            }

            fastAdapter.onLongClickListener = { v: View?, _: IAdapter<ListObject>, _: ListObject, index: Int ->
                v?.let {
                    pageID = notePageList[index].pageID
                    showDeleteDialog()
                }
                false
            }


            pageList = ArrayList()
            var i:Int = 1
            notePageList.forEach { page->
                val obj = ListObject(
                    page.data,
                    i.toString()
                )
                i++;
                pageList.add(obj)
            }

            itemAdapter.add(pageList)
            fastAdapter.notifyAdapterDataSetChanged()
            reorder_recycler_view.visibility = View.VISIBLE
            reorder_empty_layout.visibility = View.GONE
            layout_document_name.visibility = View.VISIBLE

            val dragCallback = SimpleDragCallback(ALL)
            val touchHelper = ItemTouchHelper(dragCallback)
            touchHelper.attachToRecyclerView(reorder_recycler_view)

            val listener = object : ItemTouchCallback {
                override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
                    DragDropUtil.onMove(itemAdapter, oldPosition, newPosition) // change position
                    return true
                }

                override fun itemTouchDropped(oldPosition: Int, newPosition: Int) {
                    Collections.swap(pageList, oldPosition, newPosition);
                    fastAdapter.notifyAdapterDataSetChanged()
                }
            }
        }
        else {
            reorder_recycler_view.visibility = View.GONE
            reorder_empty_layout.visibility = View.VISIBLE
        }
        reorder_progress_circular.visibility = View.GONE

        reorder_add.setOnClickListener { goToCameraActivity() }

        reorder_back.setOnClickListener { onBackPressed() }

        reorder_done.setOnClickListener {

            if(reorder_empty_layout.visibility == View.VISIBLE) {
                Toast.makeText(this, "Document is Empty!", Toast.LENGTH_SHORT).show()
            }
            else {
               createPDF()
            }
        }
    }

    private fun reorderItems( ){
        if(notePageList.isEmpty()){
            deleteNote(selectedNote)
        }
        else {
            pageList = ArrayList()
            var i = 1
            notePageList.forEach { page ->
                val obj = ListObject(
                    page.data,
                    i.toString()
                )
                i++;
                pageList.add(obj)
            }
            itemAdapter.clear()
            itemAdapter.add(pageList)
            fastAdapter.notifyAdapterDataSetChanged()
        }
    }

    private fun goToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private  fun showPDF(){
        val intent = Intent(this, PdfReviewActivity::class.java)
        intent.putExtra("noteid", noteID)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun modifyNote()= runBlocking{
        launch {
            selectedNote.title = getNoteTitle()
            selectedNote.dateModified = getDate()
            selectedNote.pdfPath = pdfPath
            db.noteDao().insertNote(selectedNote)
            db.noteDao().insertPages(notePageList)
        }
    }

    private fun retrieveNoteByID(id: Long)= runBlocking{
        launch {
            val list = db.noteDao().getNote(id)
            selectedNote = list[0].note
            notePageList = list[0].pages as MutableList<Page>
            displayData()
        }
    }

    private fun deletePage(noteID: Long, pageID: Long)= runBlocking{
        launch {
            var temp = db.noteDao().getNote(noteID)
            temp[0].note.numPages = temp[0].note.numPages - 1
            if( temp[0].note.numPages == 0){
                deleteNote(temp[0].note)
            }
            else {
                db.noteDao().deletePage(noteID, pageID)

                val list = db.noteDao().getAllPages(noteID)
                temp[0].note.thumbnail = list[0].data

                db.noteDao().insertNote(temp[0].note)

                temp = db.noteDao().getNote(noteID)
                selectedNote = temp[0].note
                notePageList.clear()
                notePageList = temp[0].pages as MutableList<Page>
                reorderItems()
            }

        }
    }

    private fun getNoteTitle(): String{
        val str: String = reorder_document_name.text.toString()
        return if(str.trim().isNotEmpty()){
            str
        } else {
            "New Document $noteID"
        }
    }

    private fun getDate(): String{
        //Get current date
        val date = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(date)
    }

    private fun showDeleteDialog() {
        if (dialogURL == null) {
            val builder = AlertDialog.Builder(this)
            val view: View = LayoutInflater.from(this).inflate(
                R.layout.delete_dialog_layout,
                layout_delete_dialog
            )
            builder.setView(view)
            dialogURL = builder.create()
            if (dialogURL!!.window != null) {
                dialogURL!!.window!!.setBackgroundDrawable(ColorDrawable(0))
            }

            view.dialog_delete_btn.setOnClickListener {
                dialogURL!!.dismiss()
                deletePage(noteID, pageID)
            }
            view.dialog_cancel_btn.setOnClickListener { dialogURL!!.dismiss() }
        }
        dialogURL!!.show()
    }

    private fun deleteNote(selectedNote: Note)= runBlocking{
        launch {
            db.noteDao().deleteNote(selectedNote)
            goToDashboard()
        }
    }

    private fun createPDF(){
        backAllowed = false
        val file = getOutputFile()
        savePDF_progress_circular.visibility = View.VISIBLE
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        class ConvertImageTask : AsyncTask<Void?, Void?, PdfDocument>() {
            override fun doInBackground(vararg params: Void?): PdfDocument {
                var bitmap: Bitmap?
                val document = PdfDocument()
                val height = 1010
                val width = 714
                var reqH: Int
                var reqW: Int
                reqW = width
                notePageList.forEach{ page->
                    bitmap = Convertors.toBitmap(page.data)
                    if(bitmap!=null) {
                        reqH = width * bitmap!!.height / bitmap!!.width

                        if (reqH < height) {
                            bitmap = Bitmap.createScaledBitmap(bitmap!!, reqW, reqH, true);
                        } else {
                            reqH = height
                            reqW = height * bitmap!!.width / bitmap!!.height
                            bitmap = Bitmap.createScaledBitmap(bitmap!!, reqW, reqH, true);
                        }
                        val out = ByteArrayOutputStream();
                        bitmap!!.compress(Bitmap.CompressFormat.WEBP, 100, out);

                        bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()));
                        bitmap = bitmap!!.copy(Bitmap.Config.RGB_565, false);

                        val pageInfo = PageInfo.Builder(reqW, reqH, 1).create()
                        val page = document.startPage(pageInfo)
                        val canvas: Canvas = page.canvas
                        val rectangle = Rect(10, 10, reqW - 10, reqH - 10)
                        canvas.drawBitmap(bitmap!!, null, rectangle, null)
                        document.finishPage(page)
                    }
                }
                return document
            }

            override fun onPostExecute(document: PdfDocument?) {
                super.onPostExecute(document)
                val fos: FileOutputStream
                try {
                    pdfPath = file?.absolutePath.toString()
                    fos = FileOutputStream(file)
                    document?.writeTo(fos)
                    document?.close()
                    fos.close()
                    savePDF_progress_circular.visibility = View.GONE
                    modifyNote()
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    backAllowed = true
                    showPDF()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        ConvertImageTask().execute()
    }

    private fun getOutputFile(): File? {
        val root = File(getExternalFilesDir(null), "PDFs")
        var isFolderCreated = true
        if (!root.exists()) {
            isFolderCreated = root.mkdir()
        }
        return if (isFolderCreated) {
            File(root, getNoteTitle() + ".pdf")
        } else {
            Toast.makeText(this, "Error Occurred!", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun goToCameraActivity(){
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("noteid",noteID)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if(backAllowed){
            super.onBackPressed()
            goToCameraActivity()
        }
        else {
            Toast.makeText(applicationContext,"PDF is being created. Please Wait!",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 101) {
            if(resultCode == Activity.RESULT_OK) {
                val filePath: String? = ImagePicker.getFilePath(data)
                if(filePath!=null){
                    val bitmap = Convertors.fileToBitmap(filePath)
                    var byteArray: ByteArray? = null
                    if (bitmap != null) {
                        byteArray = Convertors.toByteArray(bitmap)
                    }
                    if (byteArray != null) {
                        val page = Page(pageNoteID = noteID, data = byteArray)
                        notePageList.add(page)
                        modifyNoteAfterFetch()
                    }
                }
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun modifyNoteAfterFetch()= runBlocking{
        launch {
            selectedNote.title = getNoteTitle()
            selectedNote.dateModified = getDate()
            db.noteDao().insertNote(selectedNote)
            db.noteDao().insertPages(notePageList)
            notePageList.clear()
            retrieveNoteByID(noteID)
        }
    }
}
