package com.example.rickmorty.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.rickmorty.R
import com.example.rickmorty.data.CharacterRepository
import com.example.rickmorty.databinding.ActivityMainBinding
import com.example.rickmorty.db.AppDatabase
import com.example.rickmorty.db.DbService
import com.example.rickmorty.utils.Resources
import com.example.rickmorty.utils.Resources.hideKeyBoard
import com.example.rickmorty.viewmodels.CharacterViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel : CharacterViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHost = supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
        binding.bottomNavigationView.setupWithNavController(navHost.findNavController())
    }
    fun setBottomNavViewVisibility(visibility: Int){
        binding.bottomNavigationView.visibility = visibility
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        hideKeyBoard()
        return super.onTouchEvent(event)
    }
}