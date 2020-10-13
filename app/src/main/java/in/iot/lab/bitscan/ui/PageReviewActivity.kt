package `in`.iot.lab.bitscan.ui

import `in`.iot.lab.bitscan.R
import `in`.iot.lab.bitscan.entities.Page
import `in`.iot.lab.bitscan.util.Convertors
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_page_review.*


class PageReviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_review)

        continue_btn.setOnClickListener {
            startActivity(Intent(this, PdfReviewActivity::class.java))
        }

//        crop_img.setOnClickListener{
//            if (uri != null) {
//                launchImageCrop(uri)
//            }
//        }

        val data: String?  = intent.getStringExtra("data")
        if(data!=null) {
            var pageMap : HashMap<Int, Page> = Convertors.stringToMap(data) as HashMap<Int, Page>
            Log.i("Content-Bitscan",data)
            Log.i("Content-Bitscan", pageMap.size.toString())
        }
    }

    private fun launchImageCrop(uri: Uri){
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    result.uri?.let {
                        setImage(it)
                    }
                } else {

                }
            }
        }
    }

    private fun setImage(uri: Uri){
        Glide.with(this)
            .load(uri)
            .into(showPageForReview)
    }
}