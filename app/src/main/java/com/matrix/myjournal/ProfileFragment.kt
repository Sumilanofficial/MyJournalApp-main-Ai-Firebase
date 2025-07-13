package com.matrix.myjournal

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.matrix.myjournal.DataClasses.SharedViewModel
import com.matrix.myjournal.databinding.FragmentProfileBinding
import com.matrix.myjournal.databinding.UserdetailDialogBinding

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SharedViewModel
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        // Observe LiveData for real-time updates
        sharedViewModel.username.observe(viewLifecycleOwner) { name ->
            binding?.txtname?.text = name // Update the name TextView
        }

        sharedViewModel.email.observe(viewLifecycleOwner) { email ->
            binding?.txtemail?.text = email // Update the email TextView
        }

        // Add details button
        binding?.btnadddetails?.setOnClickListener {
            showUserDetailDialog()
        }

        // Example feature click listeners
        binding?.txtChangeName?.setOnClickListener {
           showUserNameDialog()
        }

        binding?.txtChangeEmail?.setOnClickListener {
          showUserEmailDialog()
        }

        binding?.faq?.setOnClickListener {
            Toast.makeText(requireContext(), "Profile features will be added soon", Toast.LENGTH_SHORT).show()
        }
        binding?.totalAmount?.setOnClickListener {
            Toast.makeText(requireContext(), "Contact matrixgmail.com", Toast.LENGTH_LONG).show()
        }

        binding?.signout?.setOnClickListener {
            Toast.makeText(requireContext(), "Features will be added soon ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUserDetailDialog() {
        val userDetailDialogBinding = UserdetailDialogBinding.inflate(layoutInflater)
        Dialog(requireContext()).apply {
            setContentView(userDetailDialogBinding.root)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()

            // Handle submit button click
            userDetailDialogBinding.buttonSubmit.setOnClickListener {
                val name = userDetailDialogBinding.editTextName.text.toString()
                val email = userDetailDialogBinding.editTextEmail.text.toString()

                if (name.isNotBlank() && email.isNotBlank()) {
                    // Update LiveData
                    sharedViewModel.username.value = name
                    sharedViewModel.email.value = email

                    dismiss()
                } else {
                    Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                }
            }

            // Handle cancel button click
            userDetailDialogBinding.buttonCancel?.setOnClickListener {
                dismiss()
            }
        }
    }
    private fun showUserNameDialog() {
        val userNameDialogBinding = UserdetailDialogBinding.inflate(layoutInflater)
        Dialog(requireContext()).apply {
            setContentView(userNameDialogBinding.root)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()
            userNameDialogBinding.editTextEmail.visibility = View.GONE


            // Handle submit button click
            userNameDialogBinding.buttonSubmit.setOnClickListener {
                val name = userNameDialogBinding.editTextName.text.toString()

                if (name.isNotBlank()) {
                    // Update LiveData
                    sharedViewModel.username.value = name

                    dismiss()
                } else {
                    Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                }
            }

            // Handle cancel button click
            userNameDialogBinding.buttonCancel?.setOnClickListener {
                dismiss()
            }
        }
    }private fun showUserEmailDialog() {
        val userNameDialogBinding = UserdetailDialogBinding.inflate(layoutInflater)
        Dialog(requireContext()).apply {
            setContentView(userNameDialogBinding.root)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()
            userNameDialogBinding.editTextName.visibility = View.GONE


            // Handle submit button click
            userNameDialogBinding.buttonSubmit.setOnClickListener {
                val email= userNameDialogBinding.editTextEmail.text.toString()

                if (email.isNotBlank()) {
                    // Update LiveData
                    sharedViewModel.email.value = email

                    dismiss()
                } else {
                    Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                }
            }

            // Handle cancel button click
            userNameDialogBinding.buttonCancel?.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null // Avoid memory leaks
    }
}
