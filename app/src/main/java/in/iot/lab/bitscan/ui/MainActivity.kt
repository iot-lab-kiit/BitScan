package `in`.iot.lab.bitscan.ui

import `in`.iot.lab.bitscan.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance();
        val user = mAuth.currentUser

    /*If the user is already signed in open dashboard else prompt to login screen */
        if(user != null){
            val dashboardIntent = Intent(this,DashboardActivity::class.java)
            startActivity(dashboardIntent);
            finish()
        }
        else {
            val signInIntent = Intent(this, SignInActivity::class.java)
            startActivity(signInIntent);
            finish()
        }
    }
}