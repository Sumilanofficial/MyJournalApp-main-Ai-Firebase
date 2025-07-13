package com.matrix.myjournal.questionresdatabase

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.matrix.myjournal.DataClasses.JournalEntry
import com.matrix.myjournal.DataClasses.SharedViewModel
import com.matrix.myjournal.Entity.QuestionsEntities
import com.matrix.myjournal.Interfaces.QuestionClickInterface
import com.matrix.myjournal.MainActivity
import com.matrix.myjournal.R
import com.matrix.myjournal.databinding.DoneDialogBinding
import com.matrix.myjournal.databinding.FragmentQuestionsBinding
import com.matrix.myjournal.utils.CloudinaryHelper
import com.rakshakhegde.stepperindicator.StepperIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class QuestionsFragment : Fragment(), QuestionClickInterface {
    private var binding: FragmentQuestionsBinding? = null
    private var currentPosition: Int = 0
    private var defaultQuestionsList = arrayListOf<QuestionsEntities>()
    private var mainActivity: MainActivity? = null
    private val responsesList: ArrayList<String> = arrayListOf()
    private var isAddingNewQuestion = false
    private var combinedResponsetitle: String? = null
    private lateinit var stepperIndicator: StepperIndicator
    private val totalquestionNo: MutableList<String> = mutableListOf()
    private val selectedImageUris: MutableList<Uri> = mutableListOf()

    private lateinit var sharedViewModel: SharedViewModel

    private val TAG = "QuestionsFragment"

    // Permission depends on Android version
    private var permission = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    } else {
        android.Manifest.permission.READ_MEDIA_IMAGES
    }

    // Request permission launcher
    private val reqPermissions = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            pickImage.launch("image/*")
        }
    }

    // Image picker launcher (multiple)
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris?.let {
            if (it.size > 2) {
                Toast.makeText(context, "You can only select up to 2 images.", Toast.LENGTH_SHORT).show()
                return@let
            }
            selectedImageUris.clear()
            selectedImageUris.addAll(it)
            updateImagePreview()
        }
    }

    // Track if images are uploading now
    private var isUploadingImages = false

    // Progress dialog shown during upload
    private var progressDialog: Dialog? = null

    // Hold uploaded image URLs for later storage
    private var uploadedImageUrls = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = activity as? MainActivity

        // Back press callback to prevent leaving during upload
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isUploadingImages) {
                    Toast.makeText(requireContext(), "Please wait, uploading images...", Toast.LENGTH_SHORT).show()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })
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

        // Initialize Cloudinary once
        CloudinaryHelper.init(requireContext())

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

        // Click on First Next button: Upload images first, then show questions UI
        binding?.firstNext?.setOnClickListener {
            if (selectedImageUris.isNotEmpty()) {
                uploadSelectedImagesThenShowQuestions()
            } else {
                showQuestionsUi()
            }
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

    // Upload images with progress, then show question UI
    private fun uploadSelectedImagesThenShowQuestions() {
        showLoading()
        setUiEnabled(false)
        isUploadingImages = true

        lifecycleScope.launch(Dispatchers.IO) {
            val imageUrls = mutableListOf<String>()
            for (uri in selectedImageUris) {
                try {
                    val url = CloudinaryHelper.uploadImage(uri, requireContext().applicationContext)
                    if (url != null) {
                        imageUrls.add(url)
                        Log.d(TAG, "Uploaded image URL: $url")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to upload image $uri", e)
                }
            }

            uploadedImageUrls = imageUrls.toMutableList()

            withContext(Dispatchers.Main) {
                hideLoading()
                setUiEnabled(true)
                isUploadingImages = false
                showQuestionsUi()
            }
        }
    }

    // Show the questions UI after initial upload
    private fun showQuestionsUi() {
        binding?.txtaddtitle?.visibility = View.GONE
        binding?.fabaddimage?.visibility = View.GONE
        binding?.firstNext?.visibility = View.GONE
        binding?.txtAddCover?.visibility = View.GONE
        binding?.journalTemplate?.visibility = View.GONE

        binding?.stepIndicator?.visibility = View.VISIBLE
        binding?.txtquestionNo?.visibility = View.VISIBLE
        binding?.etquestion?.visibility = View.VISIBLE
        binding?.txtquestion?.visibility = View.VISIBLE
        binding?.fabnext?.visibility = View.VISIBLE
        binding?.btnskip?.visibility = View.VISIBLE
    }

    // Enable or disable UI elements during upload or idle
    private fun setUiEnabled(enabled: Boolean) {
        binding?.fabnext?.isEnabled = enabled
        binding?.btnskip?.isEnabled = enabled
        binding?.fabaddimage?.isEnabled = enabled
        binding?.etquestion?.isEnabled = enabled
        binding?.firstNext?.isEnabled = enabled
    }

    // Show progress dialog
    private fun showLoading() {
        if (progressDialog == null) {
            progressDialog = Dialog(requireContext()).apply {
                setContentView(R.layout.loading_dialog) // Create this layout with a ProgressBar
                setCancelable(false)
                window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
        progressDialog?.show()
    }

    // Hide progress dialog
    private fun hideLoading() {
        progressDialog?.dismiss()
    }

    // Update image previews after picking images
    private fun updateImagePreview() {
        binding?.imageView?.let { imageView ->
            binding?.imageView1?.let { imageView1 ->
                when (selectedImageUris.size) {
                    1 -> {
                        Glide.with(this).load(selectedImageUris[0]).into(imageView)
                        imageView1.visibility = View.GONE
                    }
                    2 -> {
                        Glide.with(this).load(selectedImageUris[0]).into(imageView)
                        Glide.with(this).load(selectedImageUris[1]).into(imageView1)
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

    // Handle Next button click on questions UI
    private fun handleNextButtonClick() {
        val currentpostionadd = currentPosition + 1
        stepperIndicator.setCurrentStep(currentpostionadd)
        val responseText = binding?.etquestion?.text.toString()
        if (responseText.isNotBlank()) {
            responsesList.add(responseText)
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

    // Handle Skip button click on questions UI
    private fun handleSkipButtonClick() {
        val responseText = binding?.etquestion?.text.toString()
        if (responseText.isNotBlank()) {
            responsesList.add(responseText)
        }
        binding?.etquestion?.text?.clear()
        doYouWantToAdd()
    }

    // Save combined responses and uploaded image URLs to Firestore
    private fun storeCombinedResponses() {
        val combinedResponseText = responsesList.joinToString(" ") { it }
        val title = combinedResponsetitle ?: "Untitled"
        val time = SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().time)
        val date = finaldate()

        val entry = JournalEntry(
            title = title,
            combinedResponse = combinedResponseText,
            entryDate = date,
            entryTime = time,
            imageUrls = uploadedImageUrls
        )

        Firebase.firestore.collection("JournalEntries")
            .add(entry)
            .addOnSuccessListener {
                Log.d(TAG, "Uploaded to Firestore ✅")
            }
            .addOnFailureListener {
                Log.e(TAG, "Firestore upload failed ❌", it)
            }

        responsesList.clear()
        uploadedImageUrls.clear()
    }

    // Format current date string
    private fun finaldate(): String {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("EEEE, LLL dd")
        return simpleDateFormat.format(calendar.time)
    }

    // Show dialog when user finishes questions and entry is stored
    private fun showDoneDialog() {
        val doneDialogBinding = DoneDialogBinding.inflate(layoutInflater)
        Dialog(requireContext()).apply {
            setContentView(doneDialogBinding.root)
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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

    // Trigger journaling AI after last question or skip
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
            val answer = responsesList[i].trim()
            promptBuilder.append("Q: $question\nA: $answer\n\n")
        }

        val prompt = promptBuilder.toString()
        Log.d(TAG, "📝 Gemini Prompt:\n$prompt")

        binding?.etquestion?.setText("📝 Generating your journal entry... Please wait.")

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val model = com.google.ai.client.generativeai.GenerativeModel(
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

    // Set the current question UI
    private fun setDefaultQuestion(position: Int) {
        if (position < defaultQuestionsList.size) {
            val defaultQuestion: QuestionsEntities = defaultQuestionsList[position]
            val qnumber = "Question ${position + 1}"
            binding?.txtquestionNo?.text = "Ques${position + 1}"
            totalquestionNo.add(qnumber)
            binding?.txtquestion?.text = defaultQuestion.defaultQuestion
        }
    }

    // Change next button icon to done icon
    private fun changeFabIcon() {
        binding?.fabnext?.setIconResource(R.drawable.baseline_done_24)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    // Empty interface method implementations
    override fun showDelete(position: Int) {}
    override fun showEdit(position: Int) {}
}
