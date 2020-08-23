package `in`.iot.lab.bitscan.ui

import `in`.iot.lab.bitscan.R
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu_header.*

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            menu_bar_image.setImageBitmap(BitmapFactory.decodeFile(googleId.photoUrl?.path))
        }
        catch (e: Exception){

        }

//        MenuCompat.setGroupDividerEnabled(true)

        camera_btn.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
            finish()
        }

//    MenuCompat.setGroupDividerEnabled(menu_drawer, true)


//        mAuth = FirebaseAuth.getInstance();
//        val user = mAuth.currentUser
//
    /*If the user is already signed in open dashboard else prompt to login screen */
//        if(user != null){
//            val dashboardIntent = Intent(this,DashboardActivity::class.java)
//            startActivity(dashboardIntent);
//            finish()
//        }
//        else {
//            val signInIntent = Intent(this, SignInActivity::class.java)
//            startActivity(signInIntent)
//            finish()
//        }
//
    }
}