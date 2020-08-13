package `in`.iot.lab.bitscan.ui

import `in`.iot.lab.bitscan.R
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_dashboard.*


class DashboardActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var  googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser;
        googleUsername.text = currentUser?.displayName;

        //We need the gso to ensure the next time user logs in, the prompt to select an email is shown again
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        googleLogoutBtn.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        //Sign out from from the Auth as well as Google Client
        mAuth.signOut()
        googleSignInClient.signOut()
        val mainActivityIntent = Intent(this,MainActivity::class.java)
        startActivity(mainActivityIntent);
        finish()
    }
}