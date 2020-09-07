package `in`.iot.lab.bitscan.ui

import `in`.iot.lab.bitscan.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.menu_header.*
import org.opencv.core.Core.log
import java.io.Console


class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    lateinit var drawer : DrawerLayout
    private lateinit var username: String
    private lateinit var email: String
    private lateinit var photoURL:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        drawer = findViewById(R.id.nav_drawer)
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser;
        username = currentUser?.displayName.toString();
        email = currentUser?.email.toString();
        photoURL = currentUser?.photoUrl.toString()

        //We need the gso to ensure the next time user logs in, the prompt to select an email is shown again
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        camera_btn.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
            finish()
        }

        menu.setOnClickListener{
            drawer.openDrawer(Gravity.LEFT);
            menu_bar_name.text = username;
            menu_bar_email.text = email;
            Glide.with(this).load(photoURL).placeholder(R.drawable.google_icon).into(menu_bar_image)
        }

        // region Temporary Section
        /*addNewDocumentBtn.setOnClickListener {
            addNewDocument()
        }*/
//        addNewDocumentBtn.setOnClickListener {
//            addTempValue()
//        }
//
//        retrieveAllDocumentBtn.setOnClickListener {
//            retrieve()
//        }
//        retrieveDocumentBtn.setOnClickListener {
//            getNoteByID()
//        }
//        deleteDocumentBtn.setOnClickListener {
//            deleteAll()
//        }
        //endregion
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
//region Temporary Functions for testing
//    private fun addNewDocument(){
//        val mainActivityIntent = Intent(this,ScanActivity::class.java)
//        startActivity(mainActivityIntent);
//    }
//
//    private fun addTempValue() = runBlocking{
//        launch {
//            val map = HashMap<Int, Page>();
//            map[1] = Page(0,null,"Page 1")
//            map[2] = Page(1,null,"Page 2")
//
//            val mapData : String = Convertors.mapToString(map)
//            Log.i("Content-filter", "Adding$mapData");
//
//            val note = Note(0,"TestNote", null, false,pageData = mapData)
//            val db = NotesDatabase.getInstance(applicationContext);
//            db.noteDao().insertNote(note);
//        }
//    }
//
//    private fun retrieve(){
//        Log.i("Content-filter","Fetching");
//        var noteList = ArrayList<Note>()
//        val db= NotesDatabase.getInstance(applicationContext);
//        db.noteDao().getAllNotes().observe(this, androidx.lifecycle.Observer {
//                list -> noteList = list as ArrayList<Note>
//                Log.i("Content-filter", noteList.size.toString())
//                googleUsername.text = noteList.size.toString()
//        })
//    }
//
//
//    @SuppressLint("SetTextI18n")
//    private fun deleteAll() = runBlocking{
//        launch {
//            Log.i("Content-filter", "Deleting All");
//            val db = NotesDatabase.getInstance(applicationContext);
//            db.noteDao().deleteAllNotes();
//            googleUsername.text = "Empty"
//        }
//    }
//
//    private fun getNoteByID() {
//        class GetNotesTask :
//            AsyncTask<Void?, Void?, Note>() {
//            override fun doInBackground(vararg params: Void?): Note {
//                return NotesDatabase.getInstance(applicationContext).noteDao().getAllPages(2)
//            }
//
//            @SuppressLint("SetTextI18n")
//            override fun onPostExecute(result: Note?) {
//                super.onPostExecute(result)
//                if (result != null) {
//                    val res = result.title+ " "+result.note_id
//                    val pagedata = Convertors.stringToMap(result.pageData)
//                    googleUsername.text = res
//                    Log.i("Content-filter", "Fetched Data : ${result.pageData}");
//                    pagedata.forEach { (key, value) -> Log.i("Content-filter", "Page : $key = $value")}
//                }
//                else {
//                    googleUsername.text = "Empty List"
//                }
//            }
//        }
//        GetNotesTask().execute()
//    }
    //endregion

}