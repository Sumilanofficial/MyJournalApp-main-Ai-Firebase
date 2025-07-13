package com.matrix.myjournal.Adapters

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.matrix.myjournal.R
import com.matrix.myjournal.Entity.CombinedResponseEntity
import com.matrix.myjournal.Interfaces.ResponseClickInterface

class ResponseAdapter(
    private val context: Context,
    private var responses: List<CombinedResponseEntity>,
    private val responseClickInterface: ResponseClickInterface
) : RecyclerView.Adapter<ResponseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtResponse: TextView = view.findViewById(R.id.txtquestion)
        val ettime: TextView = view.findViewById(R.id.ettimeedit)
        val txtdate: TextView = view.findViewById(R.id.txtdate)
        val responseImage: ImageView = view.findViewById(R.id.imageView)
        val txttitle: TextView = view.findViewById(R.id.txttitle)
        val responseImage2: ImageView = view.findViewById(R.id.imageView1)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.response_items, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return responses.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val response = responses[position]
        val imageUrisJson = response.imageDataBase64
        holder.ettime.text = response.entryTime
        holder.txtdate.text = response.entryDate
        holder.txttitle.text = response.title
        holder.txtResponse.text = response.combinedResponse

        if (!imageUrisJson.isNullOrEmpty()) {
            val imageUris = parseUrisFromJson(imageUrisJson)

            // Check if image URIs are not empty
            if (imageUris.isNotEmpty()) {
                // Set image for position 1
                val firstImageUri = Uri.parse(imageUris[0])
                Glide.with(context)
                    .load(firstImageUri)
                    .into(holder.responseImage)

                // Set image for position 2 if it exists
                if (imageUris.size > 1) {
                    val secondImageUri = Uri.parse(imageUris[1])
                    Glide.with(context)
                        .load(secondImageUri)
//                    .placeholder(R.drawable.placeholder_image) // Default image while loading
//                    .error(R.drawable.error_image) // Image to show on error
                        .into(holder.responseImage2) // Assuming you have another ImageView named responseImage2
                }
            }
        }


        holder.itemView.setOnLongClickListener {
            responseClickInterface.deleteResponse(position)
            true
        }

        holder.itemView.setOnClickListener {
            responseClickInterface.updateResponse(position, response.id)
        }
    }

    private fun parseUrisFromJson(json: String): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            Log.e("ResponseAdapter", "Failed to parse image URIs from JSON", e)
            emptyList()
        }
    }

    fun updateResponses(newResponses: List<CombinedResponseEntity>) {
        this.responses = newResponses
        notifyDataSetChanged()
    }
}
