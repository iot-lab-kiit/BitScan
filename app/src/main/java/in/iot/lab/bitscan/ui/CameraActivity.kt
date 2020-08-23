package `in`.iot.lab.bitscan.ui

import `in`.iot.lab.bitscan.R
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        var flash= 0
        flash_btn.setOnClickListener {
            when(flash){
                0-> {
                    flash_btn.setImageResource(R.drawable.ic_flash_on_white_18dp)
                    flash = 1
                }
                1-> {
                    flash_btn.setImageResource(R.drawable.ic_flash_off_white_18dp)
                    flash = -1
                }
                -1-> {
                    flash_btn.setImageResource(R.drawable.ic_flash_auto_white_18dp)
                    flash = 0
                }
            }
        }

        camera_click.setOnClickListener {
            startActivity(Intent(this, PageReviewActivity::class.java))
        }
    }



}