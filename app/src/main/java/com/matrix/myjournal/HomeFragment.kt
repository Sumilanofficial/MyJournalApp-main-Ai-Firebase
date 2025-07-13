package com.matrix.myjournal

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.matrix.myjournal.Adapters.ResponseAdapter
import com.matrix.myjournal.Adapters.ShowImageAdapter
import com.matrix.myjournal.DataClasses.SharedViewModel
import com.matrix.myjournal.Entity.CombinedResponseEntity
import com.matrix.myjournal.Interfaces.ResponseClickInterface
import com.matrix.myjournal.databinding.FragmentHomeBinding
import com.matrix.myjournal.databinding.CustomUpdateDialogBinding
import com.matrix.myjournal.databinding.DeleteDialogBindingBinding
import com.matrix.myjournal.questionresdatabase.QuestionResDatabase
import java.text.SimpleDateFormat
import java.util.Calendar

class HomeFragment : Fragment(), ResponseClickInterface {
    private var binding: FragmentHomeBinding? = null
    private lateinit var sharedViewModel: SharedViewModel
    private var responseAdapter: ResponseAdapter? = null
    private var questionResDatabase: QuestionResDatabase? = null
    private val combinedResponsesList: ArrayList<CombinedResponseEntity> = arrayListOf()
    private val selectedImageUris: MutableList<Uri> = mutableListOf()
    private var showImageAdapter: ShowImageAdapter? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        selectedImageUris.clear()
        selectedImageUris.addAll(uris)
        showImageAdapter?.updateImages(selectedImageUris.toMutableList())
        Log.d("HomeFragment", "Selected images: $selectedImageUris")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        sharedViewModel.username.observe(viewLifecycleOwner) { name ->
            binding?.hi?.text = "Hi $name" // Update the name TextView
        }
        questionResDatabase = QuestionResDatabase.getInstance(requireContext())
        binding?.viewall?.setOnClickListener { findNavController().navigate(R.id.journalsFragment) }

        responseAdapter = ResponseAdapter(requireContext(), combinedResponsesList, this)
        binding?.recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.adapter = responseAdapter
        binding?.lottieprofile?.setOnClickListener{
            findNavController().navigate(R.id.profileFragment)
        }
        binding?.btnOptions?.setOnClickListener {
            Toast.makeText(requireContext(),"Feature will be added soon", Toast.LENGTH_SHORT).show()
        }



        fetchAndDisplayData()
        setupUI()
    }

    private fun fetchAndDisplayData() {
        val fetchedResponses = questionResDatabase?.questionResDao()?.getCombinedResponses() ?: emptyList()
        combinedResponsesList.clear()
        combinedResponsesList.addAll(fetchedResponses)
        Log.d("HomeFragment", "Fetched data: $combinedResponsesList")

        responseAdapter?.notifyDataSetChanged()
    }

    private fun setupUI() {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val simpleDateFormat = SimpleDateFormat("EEEE, LLL dd")
        val date = simpleDateFormat.format(calendar.time)
        binding?.txtdate?.text = date


            binding?.txtgreetings?.text = when {
                hourOfDay in 0..11 -> "Good Morning" // 12 AM to 11:59 AM
                hourOfDay in 12..17 -> "Good Afternoon" // 12 PM to 5:59 PM
                hourOfDay in 18..23 -> "Good Night" // 6 PM to 11:59 PM
                else -> "Hello"

        }

        binding?.btnadd?.setOnClickListener {
            findNavController().navigate(R.id.questionsPreferenceFragment)
        }
    }

    override fun deleteResponse(position: Int) {
        val deleteDialogBinding = DeleteDialogBindingBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext()).apply {
            setContentView(deleteDialogBinding.root)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()

            deleteDialogBinding.btnYes.setOnClickListener {
                if (position >= 0 && position < combinedResponsesList.size) {
                    val combinedResponseToDelete = combinedResponsesList[position]

                    questionResDatabase?.questionResDao()?.deleteCombinedResponse(combinedResponseToDelete.id)

                    combinedResponsesList.removeAt(position)
                    responseAdapter?.notifyItemRemoved(position)
                    responseAdapter?.notifyItemRangeChanged(position, combinedResponsesList.size)
                }
                dismiss()
            }

            deleteDialogBinding.btnNo.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun deleteImage(position: Int) {
        TODO("Not yet implemented")
    }

    override fun updateResponse(position: Int, id: Int) {
        val combinedResponse = questionResDatabase?.questionResDao()?.getCombinedResponseById(id)
        Log.d("UpdateDate", "Fetched CombinedResponse: $combinedResponse")

        if (combinedResponse != null) {
            val customUpdateDialog = CustomUpdateDialogBinding.inflate(layoutInflater)
            customUpdateDialog.etTitle.setText(combinedResponse.title ?: "")
            customUpdateDialog.txtCombinedresponse.setText(combinedResponse.combinedResponse ?: "")

            val dialog = Dialog(requireContext()).apply {
                setContentView(customUpdateDialog.root)
                window?.apply {
                    setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundDrawable(ColorDrawable(Color.WHITE))
                    decorView.setPadding(0, 0, 0, 0)
                    setWindowAnimations(R.style.DialogAnimation)
                }
                show()

                val imageStrings = parseUrisFromJson(combinedResponse.imageDataBase64 ?: "")
                val imageUris = imageStrings.map { Uri.parse(it) }.toMutableList()
                selectedImageUris.clear()
                selectedImageUris.addAll(imageUris)
                Log.d("UpdateDate", "Loaded Image URIs: $selectedImageUris")

                showImageAdapter = ShowImageAdapter(requireContext(), selectedImageUris,) { uri ->
                    showDeleteImageConfirmationDialog(uri)
                }
                // Set up the GridLayoutManager with 3 columns
                val gridLayoutManager = GridLayoutManager(requireContext(), 2)
                customUpdateDialog.recyclerView.layoutManager = gridLayoutManager

// Set up the adapter
                customUpdateDialog.recyclerView.adapter = showImageAdapter

                customUpdateDialog.fabaddimage.setOnClickListener {
                    getContent.launch("image/*")
                }

                customUpdateDialog.btnDone.setOnClickListener {
                    val updatedTitle = customUpdateDialog.etTitle.text.toString()
                    val updatedResponse = customUpdateDialog.txtCombinedresponse.text.toString()
                    val imageUrisJson = Gson().toJson(selectedImageUris.map { it.toString() })

                    Log.d("UpdateDate", "Saving Updated CombinedResponse: Title=$updatedTitle, Response=$updatedResponse, Images=$imageUrisJson")

                    combinedResponse.title = updatedTitle
                    combinedResponse.combinedResponse = updatedResponse
                    combinedResponse.imageDataBase64 = imageUrisJson

                    questionResDatabase?.questionResDao()?.updateCombinedResponse(combinedResponse)

                    combinedResponsesList[position] = combinedResponse
                    responseAdapter?.notifyItemChanged(position)

                    dismiss()
                }
            }
        } else {
            Log.d("UpdateDate", "CombinedResponse is null")
        }
    }

    private fun showDeleteImageConfirmationDialog(uri: Uri) {
        val dialogBinding = DeleteDialogBindingBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogBinding.root)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()

            dialogBinding.btnYes.setOnClickListener {
                selectedImageUris.remove(uri)
                showImageAdapter?.updateImages(selectedImageUris)
                dismiss()
            }

            dialogBinding.btnNo.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun editResponse(position: Int, entryId: Int) {
        // Implementation for editResponse
    }

    private fun parseUrisFromJson(json: String): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
