package com.matrix.myjournal.Adapters

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matrix.myjournal.Interfaces.ResponseClickInterface
import com.matrix.myjournal.R
import com.matrix.myjournal.databinding.DeleteDialogBindingBinding

class ShowImageAdapter(
    private val context: Context,
    private var images: MutableList<Uri>,
    private val onImageLongPress: (Uri) -> Unit // Callback for long press
) : RecyclerView.Adapter<ShowImageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.showimageView1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_show_images, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUri = images[position]
        Glide.with(context)
            .load(imageUri)
            .into(holder.imageView)

        // Handle long press to show delete confirmation dialog
        holder.itemView.setOnLongClickListener {
            showDeleteDialog(position)
            true // Indicates the long-click event has been handled
        }
    }

    private fun showDeleteDialog(position: Int) {
        val deleteDialogBinding = DeleteDialogBindingBinding.inflate(LayoutInflater.from(context))
        val dialog = Dialog(context).apply {
            setContentView(deleteDialogBinding.root)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()
        }

        deleteDialogBinding.btnYes.setOnClickListener {
            removeImageAt(position)
            dialog.dismiss()
        }

        deleteDialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun removeImageAt(position: Int) {
        if (position in images.indices) {
            images.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, images.size)
        }
    }

    fun updateImages(newImages: MutableList<Uri>) {
        images.clear()
        images.addAll(newImages)
        notifyDataSetChanged()
    }
}
