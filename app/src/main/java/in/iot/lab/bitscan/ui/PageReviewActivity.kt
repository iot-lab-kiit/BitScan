package `in`.iot.lab.bitscan.ui

import `in`.iot.lab.bitscan.R
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_page_review.*

class PageReviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_review)

        continue_btn.setOnClickListener {
            startActivity(Intent(this, PdfReviewActivity::class.java))
        }

    }
}