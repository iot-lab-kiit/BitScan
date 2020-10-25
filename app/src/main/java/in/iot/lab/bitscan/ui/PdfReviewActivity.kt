package `in`.iot.lab.bitscan.ui

import `in`.iot.lab.bitscan.R
import `in`.iot.lab.bitscan.data.NotesDatabase
import `in`.iot.lab.bitscan.entities.Note
import `in`.iot.lab.bitscan.entities.Page
import `in`.iot.lab.bitscan.ui.recyclerView.RecyclerView
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_pdf_review.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class PdfReviewActivity : AppCompatActivity() {
    lateinit var selectedNote: Note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_review)

        val noteID = intent.getLongExtra("noteid", -1)
        retrieveNoteByID(noteID)

        floatingAddButton.setOnClickListener{
            val intent = Intent(this, RecyclerView::class.java)
            intent.putExtra("noteid", noteID)
            startActivity(intent)
        }

        pdf_confirm.setOnClickListener {
            goToDashboard()
        }
    }

    private fun goToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun retrieveNoteByID(id: Long)= runBlocking{
        launch {
            val db = NotesDatabase.getInstance(applicationContext)
            val list = db.noteDao().getNote(id)
            selectedNote = list[0].note
            showPDF()
        }
    }

    private fun showPDF(){
        val path = selectedNote.pdfPath
        if(path.isNotEmpty()) {
            pdfView.fromFile(File(path))
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .load()

            pdf_title.text = selectedNote.title
        }

    }
}