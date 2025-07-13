package com.matrix.myjournal.questionresdatabase

import com.matrix.myjournal.Entity.QuestionsEntities

data class QuestionsList(
    var questionsList: ArrayList<QuestionsEntities>?= arrayListOf()
)
