package com.realityexpander.ktornoteapp.ui.adapters

import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.realityexpander.ktornoteapp.R
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.databinding.ItemNoteBinding
import com.realityexpander.ktornoteapp.ui.common.setDrawableColorTint
import java.text.SimpleDateFormat
import java.util.*

class NoteListAdapter: RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {


    inner class NoteViewHolder(val binding: ItemNoteBinding): RecyclerView.ViewHolder(binding.root)

    // Setup differ to track changes in the list of notes
    private val diffCallback = object: DiffUtil.ItemCallback<NoteEntity>() {
        override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    // Setup differ to work on the background thread
    private val differ = AsyncListDiffer(this, diffCallback)

    // Internal representation of the data
    var notes: List<NoteEntity>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    // Create the view elements that will be populated with note data in onBindViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return NoteViewHolder(binding)
    }

    // Called by the RecyclerView to bind the note item data to
    // a view element at the specified position.
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        val bind = holder.binding

        holder.itemView.apply {
            bind.tvTitle.text = note.title
            if(!note.isSynced) {
                bind.ivSynced.setImageResource(R.drawable.ic_cross)
                bind.tvSynced.text = "Not Synced"
            } else {
                bind.ivSynced.setImageResource(R.drawable.ic_check)
                bind.tvSynced.text = "Synced"
            }

            bind.tvDate.text = note.date

            setDrawableColorTint(bind.viewNoteColor,
                R.drawable.circle_shape,
                note.color,
                resources
            )

            // Set the onClickListener callback function for this RecyclerView item
            setOnClickListener {
                onItemClickListener?.let { onItemClick ->
                    onItemClick(note)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return notes.size
    }

    // the lambda to use for click events on the RecyclerView
    private var onItemClickListener: ((NoteEntity) -> Unit)? = null

    // sets a function to be used for the item's onClickListener
    fun setOnItemClickListener(clickListener: (NoteEntity) -> Unit) {
        this.onItemClickListener = clickListener
    }
}



































