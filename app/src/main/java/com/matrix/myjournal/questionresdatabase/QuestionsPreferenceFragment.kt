    package com.matrix.myjournal.questionresdatabase

    import DefaultQuestions
    import android.app.Dialog
    import android.os.Bundle
    import androidx.fragment.app.Fragment
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.lifecycle.ViewModelProvider
    import androidx.navigation.fragment.findNavController
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.google.gson.Gson
    import com.matrix.myjournal.Adapters.QuestionsAdapter
    import com.matrix.myjournal.DataClasses.CombinedResponse
    import com.matrix.myjournal.DataClasses.SharedViewModel
    import com.matrix.myjournal.Entity.QuestionsEntities
    import com.matrix.myjournal.Interfaces.QuestionClickInterface
    import com.matrix.myjournal.MainActivity
    import com.matrix.myjournal.R
    import com.matrix.myjournal.databinding.CustomAddquestionDialogbindingBinding
    import com.matrix.myjournal.databinding.FragmentQuestionsPreferenceBinding

    private const val ARG_PARAM1 = "param1"
    private const val ARG_PARAM2 = "param2"

    /**
     * A simple [Fragment] subclass.
     * Use the [QuestionsPreferenceFragment.newInstance] factory method to
     * create an instance of this fragment.
     */
    class
    QuestionsPreferenceFragment : Fragment(), QuestionClickInterface {
        private var binding: FragmentQuestionsPreferenceBinding? = null
        private lateinit var questionsList: ArrayList<QuestionsEntities>
        private var questionsAdapter: QuestionsAdapter? = null
        private var mainActivity: MainActivity? = null

        // Declare sharedViewModel as a lateinit var
        private lateinit var sharedViewModel: SharedViewModel
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            mainActivity = activity as? MainActivity
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            binding = FragmentQuestionsPreferenceBinding.inflate(inflater, container, false)
            return binding?.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
            questionsList = DefaultQuestions.getDefaultQuestions()
            questionsAdapter = QuestionsAdapter(requireContext(), questionsList, this)
            binding?.recyclerView?.layoutManager = LinearLayoutManager(requireContext())
            binding?.recyclerView?.adapter = questionsAdapter

            binding?.next?.setOnClickListener {
                if(questionsList.isEmpty()){
                    Toast.makeText(requireContext(), resources.getString(R.string.add_questions), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val txttitle = binding?.etTitle?.text.toString().trim()
                sharedViewModel.title = txttitle
                var questionsListArray = QuestionsList()
                questionsListArray.questionsList?.addAll(questionsList)
                var bundle = Bundle()
                bundle.putString("questions", Gson().toJson(questionsListArray))
                findNavController().navigate(R.id.questionsFragment, bundle)
            }

            binding?.addQuestion?.setOnClickListener {
                // Handle adding a new question
                val customAddQuestionDialogBinding = CustomAddquestionDialogbindingBinding.inflate(layoutInflater)
                Dialog(requireContext()).apply {
                    setContentView(customAddQuestionDialogBinding.root)
                    window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    show()

                    customAddQuestionDialogBinding.add.setOnClickListener {
                        val newQuestionText = customAddQuestionDialogBinding.editText.text.trim().toString()
                        if (newQuestionText.isNotEmpty()) {
                            val newQuestion = QuestionsEntities(newQuestionText)
                            //commented this as there is no need to add questions in the default list
//                            DefaultQuestions.addNewQuestion(newQuestion)
                            questionsList.add(newQuestion)
                            questionsAdapter?.notifyDataSetChanged()
                            dismiss()
                        }
                    }
                }
            }
        }

        override fun onPause() {
            super.onPause()
        }

        override fun showDelete(position: Int) {
            questionsList.removeAt(position)
            questionsAdapter?.notifyItemRemoved(position)
        }

        override fun showEdit(position: Int) {
            // Handle edit functionality if needed
        }

        companion object {
            @JvmStatic
            fun newInstance(param1: String, param2: String) =
                QuestionsPreferenceFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
        }
    }
