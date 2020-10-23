package `in`.iot.lab.bitscan.ui

import `in`.iot.lab.bitscan.R
import `in`.iot.lab.bitscan.data.NotesDatabase
import `in`.iot.lab.bitscan.entities.Note
import `in`.iot.lab.bitscan.ui.recyclerView.DashboardAdapter
import `in`.iot.lab.bitscan.ui.recyclerView.RecyclerView
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.delete_dialog_layout.view.*
import kotlinx.android.synthetic.main.menu_header.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.lang.Exception


class DashboardActivity : AppCompatActivity(),
                NavigationView.OnNavigationItemSelectedListener,
                DashboardAdapter.OnNoteClickListener{

    private lateinit var db: NotesDatabase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    lateinit var drawer : DrawerLayout
    private lateinit var username: String
    private lateinit var email: String
    private lateinit var photoURL:String
    private lateinit var noteList: ArrayList<Note>
    var dialogURL : AlertDialog? = null
    lateinit var context : Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        db = NotesDatabase.getInstance(applicationContext)
        context = applicationContext

        drawer = findViewById(R.id.nav_drawer)
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser;
        username = currentUser?.displayName.toString();
        email = currentUser?.email.toString();
        photoURL = currentUser?.photoUrl.toString()
        noteList = ArrayList()

        //We need the gso to ensure the next time user logs in, the prompt to select an email is shown again
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        camera_btn.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        menu.setOnClickListener{
            drawer.openDrawer(Gravity.LEFT);
            menu_bar_name.text = username;
            menu_bar_email.text = email;
            Glide.with(this).load(photoURL).placeholder(R.drawable.google_icon).into(menu_bar_image)
        }

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        dashboard_recycler_view.adapter = DashboardAdapter(noteList, this, this)
        dashboard_recycler_view.layoutManager = LinearLayoutManager(this)

        retrieveAllNotesFromDB()
    }

    private fun signOut() {
        //Sign out from from the Auth as well as Google Client
        mAuth.signOut()
        googleSignInClient.signOut()
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent);
        finish()
    }
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.logout -> {
                Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()
                signOut()
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun retrieveAllNotesFromDB(){
        db.noteDao().getAllNotes().observe(this, androidx.lifecycle.Observer {
            noteList.removeAll(noteList)

            it.forEach { NoteWithPages ->
                noteList.add(NoteWithPages.note)
            }
            removeEmptyNotes()
            if (noteList.size > 0) {
                dashboard_recycler_view.adapter!!.notifyDataSetChanged()
                dashboard_empty_layout.visibility = View.GONE
                dashboard_recycler_view.visibility = View.VISIBLE
            } else {
                dashboard_empty_layout.visibility = View.VISIBLE
                dashboard_recycler_view.visibility = View.GONE
            }
            progress_circular.visibility = View.GONE
        })
    }

    override fun onNoteClick(position: Int) {
        val clickedNoteID = noteList[position].noteID
        val pdfPath = noteList[position].pdfPath
        val intent:Intent
        intent = if(pdfPath.isEmpty()){
            Intent(this, RecyclerView::class.java)
        } else {
            Intent(this, PdfReviewActivity::class.java)
        }
        intent.putExtra("noteid", clickedNoteID)
        startActivity(intent)
    }

    override fun onNoteDelete(position: Int) {
        showDeleteDialog(position)
    }

    override fun onNoteShare(position: Int) {
        val path = noteList[position].pdfPath;
        if(path.isEmpty()){
            Toast.makeText(context,"Save the PDF of the document first!",Toast.LENGTH_SHORT).show()
        }
        else {
            sendPDF(noteList[position].pdfPath)
        }
    }

    private fun sendPDF(myFilePath: String){
        try {
            val fileWithinMyDir = File(myFilePath)
            val uri = FileProvider.getUriForFile(context,"in.iot.lab.bitscan",fileWithinMyDir)

            val share = Intent()
            share.action = Intent.ACTION_SEND
            share.type = "application/pdf"
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.putExtra(Intent.EXTRA_STREAM, uri)
            val intent = (Intent.createChooser(share, "Send document with..."))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
        catch (e: Exception){
            e.printStackTrace()
            Toast.makeText(context,"PDF Error! Re-save the document PDF or try again later.",Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteDialog(position: Int) {
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
                val clickedNote : Note = noteList[position]
                noteList.removeAt(position)
                deleteNote(clickedNote)
            }
            view.dialog_cancel_btn.setOnClickListener { dialogURL!!.dismiss() }
        }
        dialogURL!!.show()
    }


    private fun deleteNote(selectedNote: Note)= runBlocking{
        launch {
            db.noteDao().deleteNote(selectedNote)
        }
    }

    private fun removeEmptyNotes(){
        noteList.forEach{
            if(it.numPages == 0) noteList.remove(it)
        }
    }
}