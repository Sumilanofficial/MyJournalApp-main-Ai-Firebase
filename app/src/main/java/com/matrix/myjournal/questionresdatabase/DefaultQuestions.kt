import com.matrix.myjournal.Entity.QuestionsEntities

object DefaultQuestions {
    private val questionsList = arrayListOf(
        QuestionsEntities("What made you smile today?"),
        QuestionsEntities("Did you learn something new today? If so, what was it?"),
        QuestionsEntities("What are you grateful for today?"),
        QuestionsEntities("Describe a moment today when you felt at peace."),
        QuestionsEntities("What was the highlight of your day?"),
    )

    fun getDefaultQuestions(): ArrayList<QuestionsEntities> {
        return questionsList
    }
    fun getquestionslistsize():Int{
        return questionsList.size
    }

    fun addNewQuestion(newQuestion: QuestionsEntities) {
        questionsList.add(newQuestion)
    }

    fun removeQuestion(position: Int) {
        if (position in 0 until questionsList.size) {
            questionsList.removeAt(position)
        }
    }
}
