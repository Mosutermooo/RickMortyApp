package com.example.rickmorty.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rickmorty.R
import com.example.rickmorty.adapters.CharacterAdapter
import com.example.rickmorty.databinding.FragmentEpisodeViewBinding
import com.example.rickmorty.models.ApiCharacter
import com.example.rickmorty.models.Episode
import com.example.rickmorty.ui.EpisodePlayActivity
import com.example.rickmorty.utils.ConnectionLiveData
import com.example.rickmorty.utils.Resource
import com.example.rickmorty.utils.Resources
import com.example.rickmorty.utils.Resources.showSnackBar
import com.example.rickmorty.viewmodels.EpisodeViewModel
import kotlinx.coroutines.launch


class EpisodeViewFragment : Fragment() {

    private val args by navArgs<EpisodeViewFragmentArgs>()
    private lateinit var binding: FragmentEpisodeViewBinding
    private lateinit var characterViewAdapter : CharacterAdapter
    private lateinit var viewModel: EpisodeViewModel
    private lateinit var connectionLiveData: ConnectionLiveData
    private var characters : List<ApiCharacter>? = null
    private var TAG = "EpisodeViewFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEpisodeViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectionLiveData = ConnectionLiveData(requireContext())
        val episode = args.episode
        Resources.initLoadingDialog(requireContext())
        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(EpisodeViewModel::class.java)
        Log.e("episode", "$episode")
        toolBar(binding.ToolBar,
            episode.name,
            requireActivity() as AppCompatActivity)
        setupRecyclerView()
        Handler().postDelayed(
            {
                lifecycleScope.launch {
                    connectionLiveData.observe(viewLifecycleOwner){
                        if(it){
                            binding.charactersRecyclerView.visibility = View.VISIBLE
                            binding.noInternetAnimation.visibility = View.GONE
                            makeRequest(episode)
                        }else{
                            if(characters != null){
                                characterViewAdapter.differ.submitList(characters)
                                binding.charactersRecyclerView.visibility = View.VISIBLE
                                binding.noInternetAnimation.visibility = View.GONE
                            }else{
                                binding.charactersRecyclerView.visibility = View.GONE
                                binding.noInternetAnimation.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }, 200
        )
        binding.PlayEpisode.setOnClickListener {
            val intent = Intent(activity, EpisodePlayActivity::class.java)
            intent.putExtra("episode", episode)
            startActivity(intent)
        }
    }

    private fun makeRequest(episode: Episode) {
        viewModel.getMultipleCharacters(episode)
        viewModel.multipleCharacters.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success -> {
                    Resources.hideLoadingDialog()
                    characters = response.data
                    characterViewAdapter.differ.submitList(response.data)
                    Log.e("asdas", "${response.data}")
                }
                is Resource.Error -> {
                    Resources.hideLoadingDialog()
                    response.message?.let {
                        if(it != ""){
                            showSnackBar(it)
                        }
                    }
                }
                is Resource.Loading -> {
                    //Resources.loadingDialog()
                }
            }
        }


    }

    private fun setupRecyclerView() {
        characterViewAdapter = CharacterAdapter()
        binding.charactersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = characterViewAdapter
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

}