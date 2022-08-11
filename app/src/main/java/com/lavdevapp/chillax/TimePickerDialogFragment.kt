package com.lavdevapp.chillax

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment

class TimePickerDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimePickerDialog(
            requireContext(),
            requireActivity() as TimePickerDialog.OnTimeSetListener,
            0,
            0,
            true)
    }
}