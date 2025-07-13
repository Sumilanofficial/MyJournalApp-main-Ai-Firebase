package com.matrix.myjournal

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.matrix.myjournal.Adapters.ResponseAdapter
import com.matrix.myjournal.Adapters.ShowImageAdapter
import com.matrix.myjournal.DataClasses.JournalEntry
import com.matrix.myjournal.Interfaces.ResponseClickInterface
import com.matrix.myjournal.databinding.CustomUpdateDialogBinding
import com.matrix.myjournal.databinding.DeleteDialogBindingBinding
import com.matrix.myjournal.databinding.FragmentJournalsBinding
import com.matrix.myjournal.databinding.YourProgressDialogBinding
import com.matrix.myjournal.utils.CloudinaryHelper
import kotlinx.coroutines.launch

class JournalsFragment : Fragment(), ResponseClickInterface {

    private var binding: FragmentJournalsBinding? = null
    private var responseAdapter: ResponseAdapter? = null
    private val journalList = arrayListOf<JournalEntry>()

    // Holds the URLs of existing and newly uploaded images (strings)
    private val combinedImageUrls = mutableListOf<String>()

    // Holds newly selected Uris (to upload)
    private val newImageUris = mutableListOf<Uri>()

    private var showImageAdapter: ShowImageAdapter? = null
    private lateinit var getContent: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Cloudinary helper
        CloudinaryHelper.init(requireContext())

        // Register for image picking result once here
        getContent = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (!uris.isNullOrEmpty()) {
                newImageUris.addAll(uris)
                Toast.makeText(requireContext(), "${uris.size} images selected", Toast.LENGTH_SHORT).show()
                // Optionally refresh UI to show selected images if your adapter supports it
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentJournalsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        responseAdapter = ResponseAdapter(requireContext(), journalList, this)
        binding?.recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        binding?.recyclerView?.adapter = responseAdapter

        fetchAndDisplayData()
    }

    private fun fetchAndDisplayData() {
        Firebase.firestore.collection("JournalEntries")
            .get()
            .addOnSuccessListener { result ->
                journalList.clear()
                for (doc in result.documents) {
                    val entry = doc.toObject(JournalEntry::class.java)
                    if (entry != null) {
                        entry.id = doc.id
                        journalList.add(entry)
                    }
                }
                responseAdapter?.notifyDataSetChanged()
                binding?.txtNoJouranl?.visibility = if (journalList.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch journal entries", e)
            }
    }

    override fun deleteResponse(position: Int) {
        val dialogBinding = DeleteDialogBindingBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogBinding.root)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()
        }

        dialogBinding.btnYes.setOnClickListener {
            val journal = journalList[position]
            Firebase.firestore.collection("JournalEntries")
                .document(journal.id ?: "")
                .delete()
                .addOnSuccessListener {
                    journalList.removeAt(position)
                    responseAdapter?.notifyItemRemoved(position)
                    responseAdapter?.notifyItemRangeChanged(position, journalList.size)
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Delete failed", it)
                    dialog.dismiss()
                }
        }

        dialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun updateResponse(position: Int, id: Int) {
        val journal = journalList.getOrNull(position) ?: return

        combinedImageUrls.clear()
        newImageUris.clear()

        // Load existing image URLs
        val existingUrls = journal.imageUrls?.filterIsInstance<String>() ?: emptyList()
        combinedImageUrls.addAll(existingUrls)

        val customUpdateDialog = CustomUpdateDialogBinding.inflate(layoutInflater)
        customUpdateDialog.etTitle.setText(journal.title ?: "")
        customUpdateDialog.txtCombinedresponse.setText(journal.combinedResponse ?: "")

        val dialog = Dialog(requireContext()).apply {
            setContentView(customUpdateDialog.root)
            window?.apply {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setBackgroundDrawable(ColorDrawable(Color.WHITE))
                decorView.setPadding(0, 0, 0, 0)
                setWindowAnimations(R.style.DialogAnimation)
            }
            show()
        }

        showImageAdapter = ShowImageAdapter(requireContext(), combinedImageUrls) { imageUrl ->
            showDeleteImageConfirmationDialog(imageUrl)
        }
        customUpdateDialog.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        customUpdateDialog.recyclerView.adapter = showImageAdapter

        customUpdateDialog.fabaddimage.setOnClickListener {
            getContent.launch("image/*")
        }

        customUpdateDialog.btnDone.setOnClickListener {
            val updatedTitle = customUpdateDialog.etTitle.text.toString()
            val updatedText = customUpdateDialog.txtCombinedresponse.text.toString()

            // Show progress dialog
            val progressDialog = Dialog(requireContext())
            val progressBinding = YourProgressDialogBinding.inflate(layoutInflater)
            progressDialog.setContentView(progressBinding.root)
            progressDialog.setCancelable(false)
            progressDialog.show()

            lifecycleScope.launch {
                try {
                    val uploadedUrls = mutableListOf<String>()
                    for (uri in newImageUris) {
                        val url = CloudinaryHelper.uploadImage(uri, requireContext())
                        if (url != null) uploadedUrls.add(url)
                    }

                    // Merge all URLs: existing + newly uploaded
                    val allUrls = combinedImageUrls.toMutableList()
                    allUrls.addAll(uploadedUrls)

                    Firebase.firestore.collection("JournalEntries")
                        .document(journal.id ?: "")
                        .update(
                            mapOf(
                                "title" to updatedTitle,
                                "combinedResponse" to updatedText,
                                "imageUrls" to allUrls
                            )
                        )
                        .addOnSuccessListener {
                            journal.title = updatedTitle
                            journal.combinedResponse = updatedText
                            journal.imageUrls = allUrls
                            responseAdapter?.notifyItemChanged(position)
                            progressDialog.dismiss()
                            dialog.dismiss()
                            Toast.makeText(requireContext(), "Journal updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Failed to update journal", e)
                            progressDialog.dismiss()
                            Toast.makeText(requireContext(), "Failed to update journal", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    Log.e("UpdateResponse", "Image upload error", e)
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun showDeleteImageConfirmationDialog(imageUrl: String) {
        val dialogBinding = DeleteDialogBindingBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogBinding.root)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()
        }

        dialogBinding.btnYes.setOnClickListener {
            combinedImageUrls.remove(imageUrl)
            showImageAdapter?.updateImages(combinedImageUrls)
            dialog.dismiss()
        }

        dialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun deleteImage(position: Int) {
        // Not used here
    }

    override fun editResponse(position: Int, entryId: Int) {
        // Optional override if needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
