package com.example.rickmorty.ui.fragments

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rickmorty.R
import com.example.rickmorty.adapters.CharacterAdapter
import com.example.rickmorty.databinding.FragmentLocationViewBinding
import com.example.rickmorty.models.ApiCharacter
import com.example.rickmorty.models.SingleLocation
import com.example.rickmorty.ui.MainActivity
import com.example.rickmorty.utils.ConnectionLiveData
import com.example.rickmorty.utils.Resource
import com.example.rickmorty.utils.Resources.showSnackBar
import com.example.rickmorty.viewmodels.LocationViewModel


class LocationViewFragment : Fragment() {

    private val args by navArgs<LocationViewFragmentArgs>()
    private lateinit var binding: FragmentLocationViewBinding
    private lateinit var viewModel: LocationViewModel
    private lateinit var characterAdapter : CharacterAdapter
    private var characters : List<ApiCharacter>? = null
    private lateinit var connectionLiveData: ConnectionLiveData

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLocationViewBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val location = args.location
        connectionLiveData = ConnectionLiveData(requireContext())
        toolBar(binding.ToolBar, location.name, requireActivity() as AppCompatActivity)
        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(LocationViewModel::class.java)
        characterAdapter = CharacterAdapter()
        binding.charactersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = characterAdapter
        }

        characterAdapter.setOnItemClickListener { character->
            val action = LocationViewFragmentDirections.actionLocationViewFragmentToCharacterViewFragment(character)
            findNavController().navigate(action)
        }
        binding.progressBar.visibility = View.GONE
        binding.noResidents.visibility = View.GONE
        connectionLiveData.observe(viewLifecycleOwner){
            if(it){
                binding.charactersRecyclerView.visibility = View.VISIBLE
                binding.noInternetAnimation.visibility = View.GONE
                displayData(location)
            }else{
                if(characters != null){
                    characterAdapter.differ.submitList(characters)
                    binding.charactersRecyclerView.visibility = View.VISIBLE
                    binding.noInternetAnimation.visibility = View.GONE

                }else{
                    binding.progressBar.visibility = View.GONE
                    binding.charactersRecyclerView.visibility = View.GONE
                }
            }
        }


    }

    private fun displayData(location: SingleLocation) {
        viewModel.multipleLocations(location)
        viewModel.residents.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success ->{
                    characters = response.data
                    characterAdapter.differ.submitList(response.data)
                    binding.noResidents.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.charactersRecyclerView.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    response.message?.let {
                        if(it != ""){
                            showSnackBar(it)
                        }
                    }
                }
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.NoResidents ->{
                    binding.noResidents.visibility = View.VISIBLE
                    binding.charactersRecyclerView.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun showBottomBar(visibility: Int) {
        if (activity is MainActivity){
            val mainActivity = activity as MainActivity
            mainActivity.setBottomNavViewVisibility(visibility)
        }
    }

    private fun toolBar(toolbar: Toolbar, title: String, activity: AppCompatActivity){
        activity.setSupportActionBar(toolbar)
        val actionBar = activity.supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24)
            actionBar.title = title
        }

        toolbar.setNavigationOnClickListener {
            activity.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        showBottomBar(View.VISIBLE)

    }
}