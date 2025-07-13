package com.matrix.myjournal

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.matrix.myjournal.auth.GoogleSignInHelper

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var signInHelper: GoogleSignInHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        signInHelper = GoogleSignInHelper(requireContext())

        val emailField = view.findViewById<EditText>(R.id.emailEditText)
        val passwordField = view.findViewById<EditText>(R.id.passwordEditText)
        val loginBtn = view.findViewById<Button>(R.id.loginButton)
        val signupBtn = view.findViewById<Button>(R.id.signupButton)
        val googleBtn = view.findViewById<Button>(R.id.googleLoginButton)

        loginBtn.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            signInWithEmail(email, password)
        }

        signupBtn.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            signUpWithEmail(email, password)
        }

        googleBtn.setOnClickListener {
            lifecycleScope.launch {
                signInHelper.launchSignIn(requireActivity()) { success, message ->
                    if (success) {
                        goToHome()
                    } else {
                        Toast.makeText(context, "Google Sign-In Failed: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                goToHome()
            } else {
                Toast.makeText(context, "Login Failed: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signUpWithEmail(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                goToHome()
            } else {
                Toast.makeText(context, "Signup Failed: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToHome() {
        findNavController().navigate(R.id.homeFragment)
    }
}
