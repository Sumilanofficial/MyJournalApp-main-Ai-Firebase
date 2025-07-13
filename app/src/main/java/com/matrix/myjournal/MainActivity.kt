package com.matrix.myjournal

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.matrix.myjournal.databinding.ActivityMainBinding
import com.matrix.myjournal.databinding.CustomDialogBinding


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    var binding:ActivityMainBinding?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val navHostFragment = binding?.fragmentContainerView?.id?.let {
            supportFragmentManager.findFragmentById(
                it
            )
        } as NavHostFragment
        navController = navHostFragment.navController
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment,
                R.id.registerFragment,
                R.id.profileFragment,
                R.id.journalsFragment,
                R.id.insightFragment -> binding?.bottomNavigation?.visibility = View.VISIBLE
                else -> binding?.bottomNavigation?.visibility = View.GONE
            }
        }

        binding?.bottomNavigation?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home ->
                    navController.navigate(R.id.homeFragment)


                R.id.bottom_insight ->
                    navController.navigate(R.id.insightFragment)

                R.id.bottom_jouranals ->
                    navController.navigate(R.id.journalsFragment)

                R.id.bottom_profile ->
                    navController.navigate(R.id.profileFragment)

            }
            true
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment,
                R.id.journalsFragment,
                R.id.insightFragment,
                R.id.profileFragment -> {
                    binding?.bottomNavigation?.visibility = View.VISIBLE
                }
                else -> {
                    binding?.bottomNavigation?.visibility = View.GONE
                }
            }

            }
        }






    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}