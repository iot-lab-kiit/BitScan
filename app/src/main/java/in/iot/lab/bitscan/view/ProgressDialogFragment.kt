package `in`.iot.lab.bitscan.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DialogFragment
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent

@SuppressLint("ValidFragment")
class ProgressDialogFragment(private val message: String) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        val dialog = ProgressDialog(activity)
        dialog.isIndeterminate = true
        dialog.setMessage(message)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        // Disable the back button
        val keyListener =
            DialogInterface.OnKeyListener { dialog, keyCode, event -> keyCode == KeyEvent.KEYCODE_BACK }
        dialog.setOnKeyListener(keyListener)
        return dialog
    }

}