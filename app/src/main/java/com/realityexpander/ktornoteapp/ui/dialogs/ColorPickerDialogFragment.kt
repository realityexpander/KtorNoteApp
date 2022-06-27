package com.realityexpander.ktornoteapp.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.realityexpander.ktornoteapp.R
import com.realityexpander.ktornoteapp.ui.common.prependHashIfNotPresent
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class ColorPickerDialogFragment(val colorStr: String) : DialogFragment() { // DialogFragment survives screen rotation

    private var positiveListener: ((String) -> Unit)? = null
    private lateinit var colorPickerView: ColorPickerView

    fun setPositiveListener(listener: (String) -> Unit) {
        positiveListener = listener
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return ColorPickerDialog.Builder(requireContext())
            .setTitle("Choose a color")
            .setPositiveButton("Ok", object : ColorEnvelopeListener {
                override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                    positiveListener?.let { yes ->
                        envelope?.let {
                            yes(it.hexCode)
                        }
                    }
                }
            }).setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .setBottomSpace(12)
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .also {
                colorPickerView = it.colorPickerView

                it.colorPickerView?.apply {
                    setInitialColor(Color.parseColor(prependHashIfNotPresent(colorStr)))

                    //    setColorListener(object : ColorEnvelopeListener {
                    //    override fun onColorSelected(envelope: ColorEnvelope, fromUser: Boolean) {
                    //            Toast.makeText(
                    //                requireContext(),
                    //                "color changed: ${envelope.color}",
                    //                Toast.LENGTH_SHORT
                    //            ).show()
                    //        }
                    //    })
                }
            }
            .create()
    }

    fun setColor(colorStr: String) {
        val colorInt = Color.parseColor(prependHashIfNotPresent(colorStr))
        colorPickerView.selectByHsvColor(colorInt) //setInitialColor(colorInt)
    }

}