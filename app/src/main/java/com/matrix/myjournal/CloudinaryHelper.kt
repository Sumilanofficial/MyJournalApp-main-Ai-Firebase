package com.matrix.myjournal.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CloudinaryHelper {

    private var cloudinary: Cloudinary? = null

    // Call this once before uploading images, e.g. in your Fragment or Activity
    fun init(context: Context) {
        if (cloudinary != null) return

        val config: HashMap<String, String> = HashMap()
        config["cloud_name"] = ""
        config["api_key"] = ""
        config["api_secret"] = ""
        config["secure"] = "true"                       // Optional, recommended

        MediaManager.init(context, config)
        cloudinary = Cloudinary(config)

        Log.d("CloudinaryHelper", "Initialized Cloudinary with config")
    }

    // Suspend function to upload image from Uri and return the Cloudinary URL or null if fails
    suspend fun uploadImage(uri: Uri, context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val bytes = inputStream.readBytes()
                    inputStream.close()

                    // Upload image bytes to Cloudinary
                    val result = cloudinary?.uploader()?.upload(bytes, ObjectUtils.emptyMap())
                    val secureUrl = result?.get("secure_url") as? String
                    Log.d("✅ CloudinaryUpload", "Success: $secureUrl")
                    secureUrl
                } else {
                    Log.e("❌ CloudinaryUpload", "InputStream is null for URI: $uri")
                    null
                }
            } catch (e: Exception) {
                Log.e("❌ CloudinaryUpload", "Exception uploading $uri", e)
                null
            }
        }
    }
}
