package com.lavdevapp.chillax

import android.app.Dialog
import android.app.TimePickerDialog
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
            true)
    }
}