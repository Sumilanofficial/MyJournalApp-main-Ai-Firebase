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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.matrix.myjournal.Adapters.ResponseAdapter
import com.matrix.myjournal.Adapters.ShowImageAdapter
import com.matrix.myjournal.Entity.CombinedResponseEntity
import com.matrix.myjournal.Interfaces.ResponseClickInterface
import com.matrix.myjournal.databinding.FragmentJournalsBinding
import com.matrix.myjournal.databinding.CustomUpdateDialogBinding
import com.matrix.myjournal.databinding.DeleteDialogBindingBinding
import com.matrix.myjournal.questionresdatabase.QuestionResDatabase

 class JournalsFragment : Fragment(), ResponseClickInterface {
    private var binding: FragmentJournalsBinding? = null
    private var responseAdapter: ResponseAdapter? = null
    private var questionResDatabase: QuestionResDatabase? = null
    private val combinedResponsesList: ArrayList<CombinedResponseEntity> = arrayListOf()
    private val selectedImageUris: MutableList<Uri> = mutableListOf()
    private var showImageAdapter: ShowImageAdapter? = null
    private lateinit var getContent: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJournalsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUris.add(it)
                showImageAdapter?.updateImages(selectedImageUris)
            }
        }

        questionResDatabase = QuestionResDatabase.getInstance(requireContext())

        responseAdapter = ResponseAdapter(requireContext(), combinedResponsesList, this)
        binding?.recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.adapter = responseAdapter

        fetchAndDisplayData()
    }

    private fun fetchAndDisplayData() {
        val fetchedResponses = questionResDatabase?.questionResDao()?.getCombinedResponses() ?: emptyList()
        combinedResponsesList.clear()
        combinedResponsesList.addAll(fetchedResponses)
        responseAdapter?.notifyDataSetChanged()
        // Hide the TextView (set visibility to GONE) if the list is not empty
        if (combinedResponsesList.isNotEmpty()) {
            binding?.txtNoJouranl?.visibility = View.GONE  // TextView goes GONE
        } else {
            binding?.txtNoJouranl?.visibility = View.VISIBLE  // Show TextView if no data
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
        // Implement delete image logic if needed
    }

    override fun updateResponse(position: Int, id: Int) {
        val combinedResponse = questionResDatabase?.questionResDao()?.getCombinedResponseById(id)
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

                // Handle image updates similarly
                val imageStrings = parseUrisFromJson(combinedResponse.imageDataBase64 ?: "")
                val imageUris = imageStrings.map { Uri.parse(it) }.toMutableList()
                selectedImageUris.clear()
                selectedImageUris.addAll(imageUris)
                Log.d("UpdateDate", "Loaded Image URIs: $selectedImageUris")

                showImageAdapter = ShowImageAdapter(requireContext(), selectedImageUris) { uri ->
                    showDeleteImageConfirmationDialog(uri)
                }

                val gridLayoutManager = GridLayoutManager(requireContext(), 2)
                customUpdateDialog.recyclerView.layoutManager = gridLayoutManager
                customUpdateDialog.recyclerView.adapter = showImageAdapter

                customUpdateDialog.fabaddimage.setOnClickListener {
                    getContent.launch("image/*")
                }

                customUpdateDialog.btnDone.setOnClickListener {
                    val updatedTitle = customUpdateDialog.etTitle.text.toString()
                    val updatedResponse = customUpdateDialog.txtCombinedresponse.text.toString()
                    val imageUrisJson = Gson().toJson(selectedImageUris.map { it.toString() })

                    combinedResponse.title = updatedTitle
                    combinedResponse.combinedResponse = updatedResponse
                    combinedResponse.imageDataBase64 = imageUrisJson

                    questionResDatabase?.questionResDao()?.updateCombinedResponse(combinedResponse)

                    combinedResponsesList[position] = combinedResponse
                    responseAdapter?.notifyItemChanged(position)

                    dismiss()
                }
            }
        }
    }

    override fun editResponse(position: Int, entryId: Int) {
        TODO("Not yet implemented")
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

    private fun parseUrisFromJson(json: String): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
