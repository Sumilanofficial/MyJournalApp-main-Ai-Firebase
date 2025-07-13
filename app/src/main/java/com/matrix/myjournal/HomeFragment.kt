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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.matrix.myjournal.Adapters.ResponseAdapter
import com.matrix.myjournal.Adapters.ShowImageAdapter
import com.matrix.myjournal.DataClasses.JournalEntry
import com.matrix.myjournal.DataClasses.SharedViewModel
import com.matrix.myjournal.Interfaces.ResponseClickInterface
import com.matrix.myjournal.databinding.CustomUpdateDialogBinding
import com.matrix.myjournal.databinding.DeleteDialogBindingBinding
import com.matrix.myjournal.databinding.FragmentHomeBinding
import com.matrix.myjournal.databinding.YourProgressDialogBinding
import com.matrix.myjournal.utils.CloudinaryHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(), ResponseClickInterface {

    private var binding: FragmentHomeBinding? = null
    private lateinit var sharedViewModel: SharedViewModel
    private val newImageUris = mutableListOf<Uri>()

    private var responseAdapter: ResponseAdapter? = null
    private val journalEntriesList = arrayListOf<JournalEntry>()

    private var showImageAdapter: ShowImageAdapter? = null

    // Combined images: existing + newly uploaded URLs
    private val combinedImagesForAdapter = mutableListOf<String>()

    private lateinit var getContentForUpdate: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Cloudinary once
        CloudinaryHelper.init(requireContext())

        // Register launcher for selecting multiple images (only once here)
        getContentForUpdate = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (!uris.isNullOrEmpty()) {
                // Add new URIs to newImageUris and update adapter display
                newImageUris.addAll(uris)

                // Optional: Show previews of newly selected images using their URIs
                // You may want to convert Uris to URLs only after upload
                // For now, let's convert Uris to strings for adapter if it supports Uri or you can modify ShowImageAdapter accordingly

                // Since combinedImagesForAdapter holds URLs, keep them as-is
                // Here you might want to add a mechanism to show newImageUris in UI (different from URLs)
                // But to keep it simple, do nothing now. Images will be uploaded on Done button click.

                Toast.makeText(requireContext(), "${uris.size} images selected", Toast.LENGTH_SHORT).show()
            }
        }
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

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        sharedViewModel.username.observe(viewLifecycleOwner) { name ->
            binding?.hi?.text = "Hi $name"
        }

        responseAdapter = ResponseAdapter(requireContext(), journalEntriesList, this)
        binding?.recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        binding?.recyclerView?.adapter = responseAdapter

        setupUI()
        fetchAndDisplayData()

        binding?.viewall?.setOnClickListener {
            findNavController().navigate(R.id.journalsFragment)
        }

        binding?.lottieprofile?.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        binding?.btnOptions?.setOnClickListener {
            Toast.makeText(requireContext(), "Feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding?.btnadd?.setOnClickListener {
            findNavController().navigate(R.id.questionsPreferenceFragment)
        }
    }

    private fun setupUI() {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val dateFormat = SimpleDateFormat("EEEE, LLL dd", Locale.getDefault())
        binding?.txtdate?.text = dateFormat.format(calendar.time)

        binding?.txtgreetings?.text = when (hourOfDay) {
            in 0..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            else -> "Good Night"
        }
    }

    private fun fetchAndDisplayData() {
        Firebase.firestore.collection("JournalEntries")
            .get()
            .addOnSuccessListener { result ->
                journalEntriesList.clear()
                for (doc in result.documents) {
                    val entry = doc.toObject(JournalEntry::class.java)
                    if (entry != null) {
                        entry.id = doc.id
                        journalEntriesList.add(entry)
                    }
                }
                responseAdapter?.notifyDataSetChanged()
                Log.d("Firestore", "Fetched ${journalEntriesList.size} entries")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching data", e)
                Toast.makeText(requireContext(), "Failed to load entries", Toast.LENGTH_SHORT).show()
            }
    }

    override fun deleteResponse(position: Int) {
        val dialogBinding = DeleteDialogBindingBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogBinding.root)
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            show()
        }

        dialogBinding.btnYes.setOnClickListener {
            val journal = journalEntriesList[position]
            Firebase.firestore.collection("JournalEntries")
                .document(journal.id ?: "")
                .delete()
                .addOnSuccessListener {
                    journalEntriesList.removeAt(position)
                    responseAdapter?.notifyItemRemoved(position)
                    responseAdapter?.notifyItemRangeChanged(position, journalEntriesList.size)
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
        val journal = journalEntriesList.getOrNull(position) ?: return

        // Clear previous selections
        combinedImagesForAdapter.clear()
        newImageUris.clear()

        // Add existing image URLs to combined list
        val existingImageUrls = journal.imageUrls?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
        combinedImagesForAdapter.addAll(existingImageUrls)

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

        showImageAdapter = ShowImageAdapter(requireContext(), combinedImagesForAdapter) { imageUrl ->
            showDeleteImageConfirmationDialog(imageUrl)
        }
        customUpdateDialog.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        customUpdateDialog.recyclerView.adapter = showImageAdapter

        customUpdateDialog.fabaddimage.setOnClickListener {
            getContentForUpdate.launch("image/*")
        }

        customUpdateDialog.btnDone.setOnClickListener {
            val updatedTitle = customUpdateDialog.etTitle.text.toString()
            val updatedText = customUpdateDialog.txtCombinedresponse.text.toString()

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

                    // Merge existing URLs with newly uploaded URLs
                    val allImageUrls = combinedImagesForAdapter.toMutableList()
                    allImageUrls.addAll(uploadedUrls)

                    Firebase.firestore.collection("JournalEntries")
                        .document(journal.id ?: "")
                        .update(
                            mapOf(
                                "title" to updatedTitle,
                                "combinedResponse" to updatedText,
                                "imageUrls" to allImageUrls
                            )
                        )
                        .addOnSuccessListener {
                            journal.title = updatedTitle
                            journal.combinedResponse = updatedText
                            journal.imageUrls = allImageUrls
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
                    Log.e("UpdateResponse", "Error uploading images", e)
                    Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun showDeleteImageConfirmationDialog(imageUrl: String) {
        val dialogBinding = DeleteDialogBindingBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogBinding.root)
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            show()
        }

        dialogBinding.btnYes.setOnClickListener {
            combinedImagesForAdapter.remove(imageUrl)
            showImageAdapter?.updateImages(combinedImagesForAdapter)
            dialog.dismiss()
        }

        dialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun deleteImage(position: Int) {
        // Optional override if needed
    }

    override fun editResponse(position: Int, entryId: Int) {
        // Optional override if needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
