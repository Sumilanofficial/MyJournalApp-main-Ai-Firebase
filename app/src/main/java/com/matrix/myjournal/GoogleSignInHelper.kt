package com.matrix.myjournal.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption

class GoogleSignInHelper(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun launchSignIn(activity: Activity, onResult: (Boolean, String?) -> Unit) {
        try {
            Log.d("GoogleSignIn", "Preparing sign-in request...")

            val googleIdOption = GetGoogleIdOption.Builder()
                // ⚠️ Allow all Google accounts (important if it's first time)
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("912212754414-0buo46mnk2s6u3eut6q2leeulr2ks032.apps.googleusercontent.com") // Your Web Client ID
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            Log.d("GoogleSignIn", "Sending credential request...")
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleCredential.idToken
                Log.d("GoogleSignIn", "Received Google ID Token: $idToken")

                firebaseAuthWithGoogle(activity, idToken, onResult)
            } else {
                onResult(false, "Received non-Google credential")
                Log.e("GoogleSignIn", "Credential is not a Google ID token")
            }

        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Exception: ${e.localizedMessage}")
            onResult(false, e.localizedMessage)
        }
    }

    private fun firebaseAuthWithGoogle(
        activity: Activity,
        idToken: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        Log.d("GoogleSignIn", "Signing in with Firebase...")
        auth.signInWithCredential(credential).addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                Log.d("GoogleSignIn", "Firebase sign-in successful")
                onResult(true, null)
            } else {
                Log.e("GoogleSignIn", "Firebase sign-in failed: ${task.exception?.message}")
                onResult(false, task.exception?.message)
            }
        }
    }
}
