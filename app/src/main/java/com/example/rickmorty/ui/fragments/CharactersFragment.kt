package com.example.rickmorty.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AbsListView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rickmorty.R
import com.example.rickmorty.adapters.CharacterAdapter
import com.example.rickmorty.databinding.FragmentCharactersFragementBinding
import com.example.rickmorty.models.ApiCharacter
import com.example.rickmorty.models.CharacterResponse
import com.example.rickmorty.utils.ConnectionLiveData
import com.example.rickmorty.utils.Constants.QUERY_PAGE_SIZE
import com.example.rickmorty.utils.Resource
import com.example.rickmorty.utils.Resources
import com.example.rickmorty.utils.Resources.showSnackBar
import com.example.rickmorty.viewmodels.CharacterViewModel

class CharactersFragment : Fragment(R.layout.fragment_characters_fragement) {

    private lateinit var viewModel: CharacterViewModel
    private lateinit var binding: FragmentCharactersFragementBinding
    private lateinit var characterAdapter : CharacterAdapter
    private var whenShouldPaginate: Boolean = true
    private val TAG = "CharactersFragment"
    private lateinit var liveDataConnection: ConnectionLiveData
    private var characterResponse : List<ApiCharacter>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCharactersFragementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
            ).get(CharacterViewModel::class.java)
        liveDataConnection = ConnectionLiveData(requireContext())
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.ToolBar)
        setHasOptionsMenu(true)
        binding.charactersRv.visibility = View.VISIBLE


        setupRecyclerView()
        liveDataConnection.observe(viewLifecycleOwner){
            if(it){
                binding.charactersRv.visibility = View.VISIBLE
                binding.noInternetAnimation.visibility = View.GONE
                getAllCharacter()
            }else{
               if(characterResponse != null){
                   binding.charactersRv.visibility = View.VISIBLE
                   binding.noInternetAnimation.visibility = View.GONE
                   characterAdapter.differ.submitList(characterResponse)
               }else{
                   binding.charactersRv.visibility = View.GONE
                   binding.noInternetAnimation.visibility = View.VISIBLE
               }
            }
        }

        characterAdapter.setOnItemClickListener {character ->
            Log.e("characters", "$character")
            val action = CharactersFragmentDirections.actionCharactersFragment2ToCharacterViewFragment(character)
            findNavController().navigate(action)
        }

    }

    private fun getAllCharacter() {
        viewModel.getMultipleCharacters()
        viewModel.characters.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success -> {
                    hideProgressBar()
                    Log.e("page", "${viewModel.characterPage}")
                    response.data?.let {multipleCharacters ->
                        if(!isLastPage){
                            characterResponse = multipleCharacters.results.toList()
                            characterAdapter.differ.submitList(multipleCharacters.results.toList())
                            val totalPages = response.data.info.pages +1
                            isLastPage = viewModel.characterPage == totalPages
                        }

                    }
                }
                is Resource.Error -> {
                    response.message?.let {
                        hideProgressBar()
                        if(it != ""){
                            when(it){
                                "IOException"->{

                                }
                                else -> showSnackBar(it)
                            }
                        }

                    }
                }
                is Resource.Loading ->{
                    showProgressBar()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.character_menu, menu)
        val search  = menu.findItem(R.id.SearchCharacter)
        val searchQuery : androidx.appcompat.widget.SearchView =  search.actionView as androidx.appcompat.widget.SearchView
        querySearch(searchQuery)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun querySearch(search: androidx.appcompat.widget.SearchView) {
        search.queryHint = resources.getString(R.string.search_by_name)
        search.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                search(query)
                return false
            }

        })
    }

    private fun search(query: String?) {
        liveDataConnection.observe(viewLifecycleOwner){ connection ->
            if(connection){
                if(query != "" && query != null){
                    whenShouldPaginate = false
                    Log.e("query 2", "$query")
                    viewModel.search(query)
                    checkQueryResponse(query)
                }else{
                    getAllCharacter()
                    Log.e("query", "$query")
                    whenShouldPaginate = true
                }
            }
        }
    }

    private fun checkQueryResponse(query: String) {
        viewModel.searchedCharacters.observe(viewLifecycleOwner){reposnseState->
            when(reposnseState){
                is Resource.Success -> {
                    reposnseState.data?.let {
                        characterAdapter.differ.submitList(it.results)
                    }
                }
                is Resource.Error -> {
                    characterAdapter.differ.submitList(characterResponse)
                }
                is Resource.Loading ->{
                    showProgressBar()
                }
            }
        }
    }


    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling && whenShouldPaginate
            if(shouldPaginate) {
                viewModel.getMultipleCharacters()
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }



    private fun setupRecyclerView() {
        characterAdapter = CharacterAdapter()
        binding.charactersRv.apply {
            adapter = characterAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addOnScrollListener(scrollListener)
        }
    }

    private fun hideProgressBar() {
        binding.newDataLoading.visibility = View.GONE
        isLoading = false
    }

    private fun showProgressBar(){
        binding.newDataLoading.visibility = View.VISIBLE
        isLoading = true
    }

    override fun onDestroyView() {
        Resources.hideKeyBoard(requireActivity() as AppCompatActivity)
        super.onDestroyView()
    }

}