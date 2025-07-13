package com.matrix.myjournal.Adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matrix.myjournal.DataClasses.JournalEntry
import com.matrix.myjournal.Interfaces.ResponseClickInterface
import com.matrix.myjournal.R

class ResponseAdapter(
    private val context: Context,
    private var responses: ArrayList<JournalEntry>,
    private val responseClickInterface: ResponseClickInterface
) : RecyclerView.Adapter<ResponseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtResponse: TextView = view.findViewById(R.id.txtquestion)
        val etTime: TextView = view.findViewById(R.id.ettimeedit)
        val txtDate: TextView = view.findViewById(R.id.txtdate)
        val responseImage: ImageView = view.findViewById(R.id.imageView)
        val responseImage2: ImageView = view.findViewById(R.id.imageView1)
        val txtTitle: TextView = view.findViewById(R.id.txttitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.response_items, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = responses.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val response = responses[position]

        holder.txtTitle.text = response.title
        holder.txtResponse.text = response.combinedResponse
        holder.txtDate.text = response.entryDate
        holder.etTime.text = response.entryTime

        val imageUrls = response.imageUrls ?: emptyList()

        if (imageUrls.isNotEmpty()) {
            Glide.with(context).load(imageUrls[0]).into(holder.responseImage)
            if (imageUrls.size > 1) {
                Glide.with(context).load(imageUrls[1]).into(holder.responseImage2)
                holder.responseImage2.visibility = View.VISIBLE
            } else {
                holder.responseImage2.visibility = View.GONE
            }
        } else {
            holder.responseImage.visibility = View.GONE
            holder.responseImage2.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            responseClickInterface.updateResponse(position, position) // id removed
        }

        holder.itemView.setOnLongClickListener {
            responseClickInterface.deleteResponse(position)
            true
        }
    }

    fun updateResponses(newResponses: List<JournalEntry>) {
        this.responses = ArrayList(newResponses)
        notifyDataSetChanged()
    }
}
