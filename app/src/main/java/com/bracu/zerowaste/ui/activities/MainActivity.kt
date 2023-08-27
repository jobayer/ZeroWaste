package com.bracu.zerowaste.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bracu.zerowaste.R
import com.bracu.zerowaste.data.utils.setupToolbar
import com.bracu.zerowaste.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    override fun onActivityCreated(dataBinder: ActivityMainBinding) {
        binding = dataBinder
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                } else binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun init() {
        initUi()
        registerOnBackPresses {
            with(binding) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else if (!navController.popBackStack()) {
                    finishAffinity()
                }
            }
        }
    }

    private fun initUi() {
        with(binding) {
            setupToolbar(toolbar, drawerLayout)
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)
            navController = (navHostFragment as NavHostFragment).navController
            bottomNav.setupWithNavController(navController)
            navView.itemIconTintList = null
        }
    }

}