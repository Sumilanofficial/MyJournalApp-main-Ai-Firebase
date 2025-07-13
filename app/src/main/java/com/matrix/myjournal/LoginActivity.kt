package com.matrix.myjournal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.matrix.myjournal.auth.GoogleSignInHelper
import com.matrix.myjournal.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var signInHelper: GoogleSignInHelper
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        signInHelper = GoogleSignInHelper(this)

        // Email login
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            signInWithEmail(email, password)
        }

        // Email signup
        binding.signupButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            signUpWithEmail(email, password)
        }

        // Google Sign-In
        binding.googleLoginButton.setOnClickListener {
            lifecycleScope.launch {
                signInHelper.launchSignIn(this@LoginActivity) { success, message ->
                    if (success) {
                        goToHome()
                    } else {
                        Toast.makeText(this@LoginActivity, "Google Sign-In Failed: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                goToHome()
            } else {
                Toast.makeText(this, "Login Failed: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signUpWithEmail(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                goToHome()
            } else {
                Toast.makeText(this, "Signup Failed: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java) // Replace with your home screen
        startActivity(intent)
        finish()
    }
}
