package com.matrix.myjournal.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.libraries.identity.googleid.*

class GoogleSignInHelper(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    private val auth = FirebaseAuth.getInstance()

    suspend fun launchSignIn(activity: Activity, onResult: (Boolean, String?) -> Unit) {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(context.getString(R.string.default_web_client_id)) // from google-services.jso
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(context, request)
            val credential = response.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                firebaseAuthWithGoogle(activity, googleCredential.idToken, onResult)
            } else {
                onResult(false, "Not a Google Credential")
            }

        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Failed: ${e.message}")
            onResult(false, e.message)
        }
    }

    private fun firebaseAuthWithGoogle(
        activity: Activity,
        idToken: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(activity) {
            if (it.isSuccessful) {
                onResult(true, null)
            } else {
                onResult(false, it.exception?.message)
            }
        }
    }
}
