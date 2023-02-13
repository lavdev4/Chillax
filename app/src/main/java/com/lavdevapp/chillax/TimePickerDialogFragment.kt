package com.lavdevapp.chillax

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class TimePickerDialogFragment : DialogFragment() {
    //Dialog parent must implement TimePickerDialog.OnTimeSetListener
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimePickerDialog(
            requireContext(),
            requireActivity() as TimePickerDialog.OnTimeSetListener,
            0,
            0,
            true
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                setTitle(getString(R.string.timer_dialog_title))
                setMessage(getString(R.string.timer_dialog_message))
            }
            setButton(DialogInterface.BUTTON_POSITIVE, "Start", this)
            setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", this)
        }
    }
}