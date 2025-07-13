package com.matrix.myjournal.questionresdatabase

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.matrix.myjournal.DataClasses.SharedViewModel
import com.matrix.myjournal.DataClasses.SingleResponse
import com.matrix.myjournal.Entity.CombinedResponseEntity
import com.matrix.myjournal.Entity.QuestionsEntities
import com.matrix.myjournal.MainActivity
import com.matrix.myjournal.R
import com.matrix.myjournal.databinding.DoneDialogBinding
import com.matrix.myjournal.databinding.FragmentQuestionsBinding
import com.google.gson.Gson
import com.matrix.myjournal.Adapters.QuestionsAdapter
import com.matrix.myjournal.Interfaces.QuestionClickInterface
import com.rakshakhegde.stepperindicator.StepperIndicator
import com.bumptech.glide.Glide
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar

class QuestionsFragment : Fragment(), QuestionClickInterface {
    private var binding: FragmentQuestionsBinding? = null
    private var currentPosition: Int = 0
    private var defaultQuestionsList = arrayListOf<QuestionsEntities>()
    private var mainActivity: MainActivity? = null
    private var questionResDatabase: QuestionResDatabase? = null
    private val responsesList: ArrayList<SingleResponse> = arrayListOf()
    private var isAddingNewQuestion = false
    private var combinedResponsetitle: String? = null
    private lateinit var stepperIndicator: StepperIndicator
    private val totalquestionNo: MutableList<String> = mutableListOf()
    private val selectedImageUris: MutableList<Uri> = mutableListOf()

    private lateinit var sharedViewModel: SharedViewModel

    var permission = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    } else {
        android.Manifest.permission.READ_MEDIA_IMAGES
    }

    private val TAG = "QuestionsFragment"

    var reqPermissions = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            pickImage.launch("image/*")
        }
    }

    var pickImage = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris?.let {
            if (it.size > 2) {
                Log.e(TAG, "Please select up to 2 images only.")
                Toast.makeText(context, "You can only select up to 2 images.", Toast.LENGTH_SHORT).show()
                return@let
            }
            selectedImageUris.clear()
            selectedImageUris.addAll(it)
            updateImagePreview()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = activity as? MainActivity
        questionResDatabase = QuestionResDatabase.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQuestionsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        combinedResponsetitle = sharedViewModel.title

        arguments?.let {
            val questions = it.getString("questions")
            val qList = Gson().fromJson(questions, QuestionsList::class.java)
            defaultQuestionsList.addAll(qList.questionsList as ArrayList<QuestionsEntities>)
            if (defaultQuestionsList.isNotEmpty()) {
                setDefaultQuestion(currentPosition)
            }
        }

        stepperIndicator = binding?.stepIndicator!!
        stepperIndicator.setStepCount(defaultQuestionsList.size + 1)

        val stepLabels = (1..defaultQuestionsList.size).map { "Ques $it" } + "Done"
        stepperIndicator.setLabels(stepLabels.toTypedArray())

        binding?.firstNext?.setOnClickListener {
            binding?.txtaddtitle?.visibility=View.GONE
            binding?.fabaddimage?.visibility=View.GONE
            binding?.firstNext?.visibility=View.GONE
            binding?.txtAddCover?.visibility=View.GONE
            binding?.journalTemplate?.visibility=View.GONE
            binding?.stepIndicator?.visibility= View.VISIBLE
            binding?.txtquestionNo?.visibility= View.VISIBLE
             binding?.etquestion?.visibility= View.VISIBLE
              binding?.txtquestion?.visibility= View.VISIBLE
              binding?.fabnext?.visibility= View.VISIBLE
                binding?.btnskip?.visibility= View.VISIBLE

        }

        binding?.fabnext?.setOnClickListener {
            handleNextButtonClick()
        }

        binding?.btnskip?.setOnClickListener {
            handleSkipButtonClick()
        }

        binding?.fabaddimage?.setOnClickListener {
            reqPermissions.launch(permission)
        }
    }

    private fun handleNextButtonClick() {
        val currentpostionadd = currentPosition + 1
        stepperIndicator.setCurrentStep(currentpostionadd)
        val responseText = binding?.etquestion?.text.toString()
        if (responseText.isNotBlank()) {
            responsesList.add(SingleResponse(responseText))
            binding?.etquestion?.text?.clear()
        }

        if (isAddingNewQuestion) {
            storeCombinedResponses()
            showDoneDialog()
        } else if (currentPosition < defaultQuestionsList.size - 1) {
            currentPosition++
            setDefaultQuestion(currentPosition)
        } else if (currentPosition == defaultQuestionsList.size - 1) {
            doYouWantToAdd()
        }
    }

    private fun handleSkipButtonClick() {
        val responseText = binding?.etquestion?.text.toString()
        if (responseText.isNotBlank()) {
            responsesList.add(SingleResponse(responseText))
        }
        binding?.etquestion?.text?.clear()
        doYouWantToAdd()
    }

    private fun updateImagePreview() {
        binding?.imageView?.let { imageView ->
            binding?.imageView1?.let { imageView1 ->
                when (selectedImageUris.size) {
                    1 -> {
                        Glide.with(this)
                            .load(selectedImageUris[0])
                            .into(imageView)
                        imageView1.visibility = View.GONE
                    }
                    2 -> {
                        Glide.with(this)
                            .load(selectedImageUris[0])
                            .into(imageView)
                        Glide.with(this)
                            .load(selectedImageUris[1])
                            .into(imageView1)
                        imageView1.visibility = View.VISIBLE
                    }
                    else -> {
                        imageView.visibility = View.GONE
                        imageView1.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun storeCombinedResponses() {
        val combinedResponseText = responsesList.joinToString("") { it.response }
        val title = combinedResponsetitle ?: "Untitled"
        val calendar = Calendar.getInstance()
        val date = calendar.time
        val timeFormat = SimpleDateFormat("HH:mm:ss")
        val time = timeFormat.format(date)
        val finaldate = finaldate()
        val imageUrisJson = Gson().toJson(selectedImageUris.map { it.toString() })
        Log.e(TAG, "imageUrisJson $imageUrisJson")
        questionResDatabase?.questionResDao()?.insertCombinedResponse(
            CombinedResponseEntity(
                entryDate = finaldate,
                entryTime = time,
                title = title,
                combinedResponse = combinedResponseText,
                imageDataBase64 = imageUrisJson
            )
        )
        responsesList.clear()
    }

    private fun finaldate(): String {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("EEEE, LLL dd")
        return simpleDateFormat.format(calendar.time)
    }

    private fun showDoneDialog() {
        val doneDialogBinding = DoneDialogBinding.inflate(layoutInflater)
        Dialog(requireContext()).apply {
            setContentView(doneDialogBinding.root)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()

            doneDialogBinding.btndone.setOnClickListener {
                dismiss()
                val intent = Intent(requireContext(), MainActivity::class.java)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    requireContext(),
                    R.anim.fade_in,
                    R.anim.fade_out
                )
                startActivity(intent, options.toBundle())
                activity?.finishAffinity()
            }
        }
    }

    private fun doYouWantToAdd() {
        binding?.txtquestionNo?.text = "Add"
        binding?.btnskip?.visibility = View.GONE
        binding?.txtquestion?.text = "Do you want to add something more?"
        changeFabIcon()
        isAddingNewQuestion = true

        val promptBuilder = StringBuilder()
        promptBuilder.append("You are a friendly journaling assistant for a mobile app.\n")
        promptBuilder.append("Based on the following daily Q&A entries, write a short, meaningful journal entry that:\n")
        promptBuilder.append("- Feels personal and emotional 💖\n")
        promptBuilder.append("- Is written in first person (as if the user is writing it)\n")
        promptBuilder.append("- Is structured into short sections with relevant titles and emojis\n")
        promptBuilder.append("- Encourages positivity and reflection ✨\n\n")

        for (i in responsesList.indices) {
            val question = DefaultQuestions.getDefaultQuestions().getOrNull(i)?.defaultQuestion ?: "Question ${i + 1}"
            val answer = responsesList[i].response.trim()
            promptBuilder.append("Q: $question\nA: $answer\n\n")
        }

        val prompt = promptBuilder.toString()
        Log.d(TAG, "📝 Gemini Prompt:\n$prompt")

        // Show loading message
        binding?.etquestion?.setText("📝 Generating your journal entry... Please wait.")

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val model = GenerativeModel(
                    modelName = "gemini-2.5-flash",
                    apiKey = "AIzaSyBQ-RZNZitBUx_tNbn5Hz4G4Iffvl06vXI"
                )

                val response = model.generateContent(prompt)
                val story = response.text ?: "No story generated."

                withContext(Dispatchers.Main) {
                    binding?.etquestion?.setText(story)
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to generate story in doYouWantToAdd()", e)
                withContext(Dispatchers.Main) {
                    binding?.etquestion?.setText("⚠️ Story generation failed. You can write your own version below.")
                    Toast.makeText(requireContext(), "Gemini error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun setDefaultQuestion(position: Int) {
        if (position < defaultQuestionsList.size) {
            val defaultQuestion: QuestionsEntities = defaultQuestionsList[position]
            val qnumber = "Question ${position + 1}"
            binding?.txtquestionNo?.text = "Ques${position + 1}"
            totalquestionNo.add(qnumber)
            binding?.txtquestion?.text = defaultQuestion.defaultQuestion
        }
    }

    private fun changeFabIcon() {
        binding?.fabnext?.setIconResource(R.drawable.baseline_done_24)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun showDelete(position: Int) {
        // Implement deletion logic if needed
    }

    override fun showEdit(position: Int) {
        // Implement edit logic if needed
    }
}
