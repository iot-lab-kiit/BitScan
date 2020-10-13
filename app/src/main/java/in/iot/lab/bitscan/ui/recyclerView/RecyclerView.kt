package `in`.iot.lab.bitscan.ui.recyclerView

import `in`.iot.lab.bitscan.R
import `in`.iot.lab.bitscan.data.NotesDatabase
import `in`.iot.lab.bitscan.entities.Note
import `in`.iot.lab.bitscan.entities.Page
import `in`.iot.lab.bitscan.ui.DashboardActivity
import `in`.iot.lab.bitscan.util.Convertors
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.drag.ItemTouchCallback
import com.mikepenz.fastadapter.drag.SimpleDragCallback
import com.mikepenz.fastadapter.drag.SimpleDragCallback.Companion.ALL
import com.mikepenz.fastadapter.utils.DragDropUtil
import kotlinx.android.synthetic.main.activity_recycler_view.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class RecyclerView : AppCompatActivity() {

    lateinit var pageList : ArrayList<ListObject>
    lateinit var pageMap : HashMap<Int, Page>
    var id: Int = -1
    var fetch:Boolean = false
    lateinit var selectedNote: Note
    var pdfBeingCreated: Boolean = false
    lateinit var pdfPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)

        val sharedPreference =  getSharedPreferences("BITSCAN_DATA", Context.MODE_PRIVATE)
        val data: String? = sharedPreference.getString("data", null)
        id = sharedPreference.getInt("id", -1)
        fetch = sharedPreference.getBoolean("fetch", false)

        if(fetch){
            getNoteByID(id)
        }
        else if(!data.isNullOrEmpty()) {
            pageMap = Convertors.stringToMap(data) as HashMap<Int, Page>
            displayData()
        }
    }

    private fun displayData(){
        if(pageMap.size > 0) {
            val itemAdapter = ItemAdapter<ListObject>()
            //create the managing FastAdapter, by passing in the itemAdapter
            val fastAdapter = FastAdapter.with(itemAdapter)

            reorder_recycler_view.apply {
                layoutManager = GridLayoutManager(this@RecyclerView, 2)
                adapter = fastAdapter
            }

            pageList = ArrayList()

            for (pageNum in pageMap.keys) {
                val obj = ListObject(
                    pageMap[pageNum]?.pageData.toString(),
                    (pageNum + 1).toString()
                )
                pageList.add(obj)
            }

            itemAdapter.add(pageList)
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

    private fun goToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
    }

    private fun saveNewNote(context: Context)= runBlocking{
        launch {
            val mapData : String = Convertors.mapToString(pageMap)
            val title = getNoteTitle()
            val date =  getDate()
            val note = Note(
                title = title,
                dateModified = date,
                onCloud = false,
                pageData = mapData,
                thumbnail = pageList.get(0).imagePath,
                pdfPath = pdfPath
            )
            val db = NotesDatabase.getInstance(applicationContext);
            db.noteDao().insertNote(note);
        }
    }

    private fun getNoteByID(id: Int) {
        class GetNotesTask :
            AsyncTask<Void?, Void?, Note>() {
            override fun doInBackground(vararg params: Void?): Note {
                return NotesDatabase.getInstance(applicationContext).noteDao().getNote(id)
            }

            @SuppressLint("SetTextI18n")
            override fun onPostExecute(result: Note?) {
                super.onPostExecute(result)
                if (result != null) {
                    selectedNote = result
                    Log.i("Content-filter", "Fetched Data : ${result.pageData}");
                    pageMap = Convertors.stringToMap(result.pageData) as HashMap<Int, Page>
                    reorder_document_name.setText(selectedNote.title)
                    displayData()
                }
            }
        }
        GetNotesTask().execute()
    }

    private fun updateNote()= runBlocking{
        launch {
            val mapData : String = Convertors.mapToString(pageMap)
            val title = getNoteTitle()
            val date =  getDate()
            selectedNote.title = title
            selectedNote.dateModified = date
            selectedNote.pageData = mapData
            selectedNote.thumbnail = pageList.get(0).imagePath
            selectedNote.pdfPath = pdfPath
            val db = NotesDatabase.getInstance(applicationContext);
            db.noteDao().updateNote(selectedNote)
        }
    }

    private fun getNoteTitle(): String{
        val str: String = reorder_document_name.text.toString()
        return if(str.trim().isNotEmpty()){
            str
        } else {
            "New Document"
        }
    }

    private fun getDate(): String{
        //Get current date
        val date = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(date)
    }

    private fun createPDF(){
        val file = getOutputFile()
        savePDF_progress_circular.visibility = View.VISIBLE

        class ConvertImageTask : AsyncTask<Void?, Void?, PdfDocument>() {
            override fun doInBackground(vararg params: Void?): PdfDocument {
                var bitmap: Bitmap
                val document = PdfDocument()
                val height = 1010
                val width = 714
                var reqH: Int
                var reqW: Int
                reqW = width
                for (i in 0 until pageMap.size) {
                    bitmap = BitmapFactory.decodeFile(pageMap[i]?.pageData);
                    reqH = width * bitmap.height / bitmap.width

                    if (reqH < height) {
                        bitmap = Bitmap.createScaledBitmap(bitmap, reqW, reqH, true);
                    } else {
                        reqH = height
                        reqW = height * bitmap.width / bitmap.height
                        bitmap = Bitmap.createScaledBitmap(bitmap, reqW, reqH, true);
                    }
                    val out = ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 50, out);
                    bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()));
                    bitmap = bitmap.copy(Bitmap.Config.RGB_565, false);

                    val pageInfo = PageInfo.Builder(reqW, reqH, 1).create()
                    val page = document.startPage(pageInfo)
                    val canvas: Canvas = page.canvas
                    val rectangle = Rect(10, 10, reqW - 10, reqH - 10)
                    canvas.drawBitmap(bitmap, null, rectangle, null)
                    document.finishPage(page)
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
                    if (fetch) {
                        updateNote()
                    } else {
                        saveNewNote(applicationContext)
                    }
                    goToDashboard()
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
}
