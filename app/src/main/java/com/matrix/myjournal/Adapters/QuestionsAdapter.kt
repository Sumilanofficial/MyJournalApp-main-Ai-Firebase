package com.matrix.myjournal.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.matrix.myjournal.R
import com.matrix.myjournal.Interfaces.QuestionClickInterface
import com.matrix.myjournal.Entity.QuestionsEntities

class QuestionsAdapter(
    var context: Context,
    var questions: ArrayList<QuestionsEntities>,
    var questionClickInterface: QuestionClickInterface
) : RecyclerView.Adapter<QuestionsAdapter.ViewHolder>() {

    private var selectedItemPosition: Int = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtQuestion: TextView = view.findViewById(R.id.txtquestion)
        var txtQuestionNo: TextView = view.findViewById(R.id.txtquestionNo)
        var btnedit: Button = view.findViewById(R.id.btnEdit)
        var btndelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_questionadapter, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val questionNo = position + 1
        holder.txtQuestion.text = questions[position].defaultQuestion
        holder.txtQuestionNo.text = questionNo.toString()

        holder.itemView.setOnClickListener {
            selectedItemPosition = position
            notifyDataSetChanged()
        }
        if (selectedItemPosition == position) {
            holder.btndelete.visibility = View.VISIBLE
        } else {
            holder.btndelete.visibility = View.GONE
        }
        holder.btndelete.setOnClickListener {
            questionClickInterface.showDelete(position)
        }
    }
}
