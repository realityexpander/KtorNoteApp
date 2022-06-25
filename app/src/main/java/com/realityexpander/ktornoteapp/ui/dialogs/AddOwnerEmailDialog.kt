package com.realityexpander.ktornoteapp.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.realityexpander.ktornoteapp.R

class AddOwnerEmailDialog : DialogFragment() { // DialogFragment survives screen rotation

    private var positiveListener: ((String) -> Unit)? = null  // this is destroyed on configuration change, so we need to restore it in the fragment onViewCreated

    fun setPositiveListener(listener: (String) -> Unit) {
        positiveListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val clNoteContainer = requireActivity().findViewById<ConstraintLayout>(R.id.clNoteContainer)

        val addOwnerEditText = LayoutInflater.from(requireContext()).inflate(
            R.layout.edit_text_email,
            clNoteContainer,  // which view to attach the dialog to (the container)
            false
        ) as TextInputLayout

        return MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_add_person)
            .setTitle("Add Owner to Note")
            .setMessage("Enter an E-Mail of a person you want to share the note with.\n\n" +
                    "This person will be able to read and edit the note and must have an account on this app.")
            .setView(addOwnerEditText)
            .setPositiveButton("Add") { _, _ ->
                val email = addOwnerEditText.findViewById<EditText>(R.id.etAddOwnerEmail).text.toString()

                positiveListener?.let { yes ->
                    yes(email)
                }
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()

    }

}