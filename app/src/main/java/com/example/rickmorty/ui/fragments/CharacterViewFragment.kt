package com.example.rickmorty.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.rickmorty.R
import com.example.rickmorty.adapters.CharacterViewEpisodeAdapter
import com.example.rickmorty.databinding.FragmentCharacterViewBinding
import com.example.rickmorty.models.ApiCharacter
import com.example.rickmorty.models.Episode
import com.example.rickmorty.utils.ConnectionLiveData
import com.example.rickmorty.utils.Resource
import com.example.rickmorty.utils.Resources
import com.example.rickmorty.utils.Resources.showSnackBar
import com.example.rickmorty.viewmodels.CharacterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CharacterViewFragment : Fragment(R.layout.fragment_character_view) {
    private lateinit var binding : FragmentCharacterViewBinding
    private val args by navArgs<CharacterViewFragmentArgs>()
    private lateinit var viewModel: CharacterViewModel
    private var isAddedToFavorite: Boolean = false
    private var episodes: List<Episode>? = null
    private lateinit var episodeAdapter: CharacterViewEpisodeAdapter
    private var TAG = "CharacterViewFragment"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCharacterViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.notify.visibility = View.GONE
        val liveDataConnection = ConnectionLiveData(requireContext())
        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(CharacterViewModel::class.java)
        val character = args.character
        Log.e("character", "$character")
        Resources.toolBar(binding.ToolBar,
            character.name,
            null,
            requireActivity() as AppCompatActivity)

        displayData(character)

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.alreadyAddedToFavorite(character.id).let {
                if(it != null){
                    if(it.id == character.id){
                        isAddedToFavorite = true
                        binding.Favorite.setImageDrawable(
                            ContextCompat.getDrawable(requireContext(),
                                R.drawable.ic_baseline_favorite_24)
                        )
                        Log.e("Character form db", "$it" )
                    }
                }

            }
        }

        setUpEpisodeRecyclerView()
        liveDataConnection.observe(viewLifecycleOwner){
            if(it){
                binding.EpisodesRV.visibility = View.VISIBLE
                binding.noInternetAnimation.visibility = View.GONE
                getMultipleEpisodes(character)
            }else{
                if(episodes != null){
                    episodeAdapter.differ.submitList(episodes)
                    binding.EpisodesRV.visibility = View.VISIBLE
                    binding.noInternetAnimation.visibility = View.GONE
                }else{
                    binding.EpisodesRV.visibility = View.GONE
                    binding.noInternetAnimation.visibility = View.VISIBLE
                }
            }
        }


        episodeAdapter.setOnItemClickListener {episode->
            val action = CharacterViewFragmentDirections.actionCharacterViewFragmentToEpisodeViewFragment(episode)
            findNavController().navigate(action)
        }

        binding.Favorite.setOnClickListener{
           if(isAddedToFavorite){
              removeFromRoomDB(character)
              return@setOnClickListener
           }

           if(!isAddedToFavorite){
               viewModel.saveFavoriteCharacter(character)
               showTopTV(resources.getString(R.string.added_to_favorite))
               binding.Favorite.setImageDrawable(
                   ContextCompat.getDrawable(requireContext(),
                       R.drawable.ic_baseline_favorite_24)
               )
               isAddedToFavorite = true
           }
        }
    }



    private fun getMultipleEpisodes(character: ApiCharacter) {
        viewModel.getMultipleEpisodes(character)
        viewModel.multipleEpisodes.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success -> {
                    Log.e("mutliple Episodes", "$it")
                    episodes = it.data
                    episodeAdapter.differ.submitList(episodes)
                }
                is Resource.Error ->{
                    it.message?.let { message->
                        if(message != ""){
                            showSnackBar(message)
                        }
                    }
                }
                is Resource.Loading ->{

                }
            }
        }
    }

    private fun displayData(character: ApiCharacter) {
        Glide.with(requireContext())
            .load(character.image)
            .centerCrop().into(binding.characterImage)
        binding.NameAndAliveIndicator.text = character.name
        checkCharacterStatus(character.status)
        binding.TypeAndGender.text = "{${character.species} - ${character.gender}}"
        binding.Location.text = "{${resources.getString(R.string.location)} - ${character.location.name}}"
        binding.Episodes.text = "${resources.getString(R.string.episodes)}(${character.episode.size})"

    }

    private fun setUpEpisodeRecyclerView() {
        episodeAdapter = CharacterViewEpisodeAdapter()
        binding.EpisodesRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = episodeAdapter
        }
    }

    private fun checkCharacterStatus(status: String){
        when(status){
            "Alive" -> {
                binding.NameAndAliveIndicator.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_alive,0,0,0
                )
                binding.StatusText.text = "($status)"
                binding.StatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            }
            "Dead" -> {
                binding.NameAndAliveIndicator.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_dead,0,0,0
                )
                binding.StatusText.text = "($status)"
                binding.StatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            }
            "unknown" -> {
                binding.NameAndAliveIndicator.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_unkonwn,0,0,0
                )
                binding.StatusText.text = "($status)"
                binding.StatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            }
        }
    }

    private fun showTopTV(tittle: String) {
        binding.notify.text = tittle
        binding.notify.visibility = View.VISIBLE
        Handler().postDelayed(
            {
                binding.notify.visibility = View.GONE
            }, 2000
        )

    }


    private fun removeFromRoomDB(character: ApiCharacter) {
        viewModel.deleteSingleCharacter(character.id)
        binding.Favorite.setImageDrawable(
            ContextCompat.getDrawable(requireContext(),
                R.drawable.ic_baseline_favorite_border_24)
        )
        isAddedToFavorite = false
        showTopTV(resources.getString(R.string.removed_from_favorite))
    }






}