package com.matrix.myjournal

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.matrix.myjournal.databinding.ActivityRegistrationBinding

class RegistrationActivity : AppCompatActivity() {
    var binding:ActivityRegistrationBinding?=null
    var navController:NavController?=null
    var navHostFragment:NavHostFragment?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registrationActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding?.customToolbar?.btnskip?.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            val options =
                ActivityOptionsCompat.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
            startActivity(intent, options.toBundle())
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController?.navigateUp() == true ||super.onSupportNavigateUp()
    }


}