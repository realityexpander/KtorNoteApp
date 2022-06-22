package com.realityexpander.ktornoteapp.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.realityexpander.ktornoteapp.R
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class NoteListAdapter: RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {

// old way of doing it
//    inner class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
//
//        fun bind(note: NoteEntity) {
//
//        }
//    }

    inner class NoteViewHolder(val binding: ItemNoteBinding): RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object: DiffUtil.ItemCallback<NoteEntity>() {
        override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

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

// old way
//        return NoteViewHolder(
//            LayoutInflater.from(parent.context).inflate(
//                R.layout.item_note,
//                parent,
//                false
//            )
//        )
    }

    // Called by the RecyclerView to bind the data to view element at the specified position.
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

            val dateFormat = SimpleDateFormat("MM/dd/yyyy, HH:mm", Locale.getDefault())
            val dateString = dateFormat.format(note.date)
            bind.tvDate.text = dateString

            val drawable = ResourcesCompat.getDrawable(resources, R.drawable.circle_shape, null)
            drawable?.let {
                val wrappedDrawable = DrawableCompat.wrap(it)
                val color = Color.parseColor("#${note.color}")
                DrawableCompat.setTint(wrappedDrawable, color)
                bind.viewNoteColor.background = it // wrappedDrawable

            }
        }

        // Set the onClick function for this RecyclerView item
        setOnItemClickListener {
            onItemClickListener?.let { onItemClick ->
                onItemClick(note)
            }
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    // define lambda to handle click events on the RecyclerView
    private var onItemClickListener: ((NoteEntity) -> Unit)? = null

    fun setOnItemClickListener(clickListener: (NoteEntity) -> Unit) {
        this.onItemClickListener = clickListener
    }
}


































