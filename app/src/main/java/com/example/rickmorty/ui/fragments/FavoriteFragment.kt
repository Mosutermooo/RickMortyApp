package com.example.rickmorty.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rickmorty.R
import com.example.rickmorty.adapters.CharacterAdapter
import com.example.rickmorty.adapters.FavoriteCharacterAdapter
import com.example.rickmorty.databinding.FragmentFavoriteBinding
import com.example.rickmorty.utils.Resources
import com.example.rickmorty.viewmodels.CharacterViewModel


class FavoriteFragment : Fragment(R.layout.fragment_favorite) {

    private lateinit var binding: FragmentFavoriteBinding
    private lateinit var viewModel: CharacterViewModel
    private lateinit var characterAdapter: FavoriteCharacterAdapter
    private var TAG = "FavoriteFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(CharacterViewModel::class.java)
        setUpRecyclerView()

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.ToolBar)
        setHasOptionsMenu(true)

        viewModel.getFavoriteCharacters().observe(viewLifecycleOwner){
            characterAdapter.differ.submitList(it.toList())
        }
        characterAdapter.setOnDeleteListener {
            viewModel.deleteSingleCharacter(it.id)
        }
        characterAdapter.setOnItemClickListener {
            val action = FavoriteFragmentDirections.actionFavoriteFragment2ToCharacterViewFragment(it)
            findNavController().navigate(action)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.favorite_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.deleteAllCharacters ->{
                viewModel.deleteAllCharacters()
            }
        }
        return super.onOptionsItemSelected(item)
    }



    private fun setUpRecyclerView() {
        characterAdapter = FavoriteCharacterAdapter()
        binding.favoriteCharacters.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = characterAdapter
        }
    }

}