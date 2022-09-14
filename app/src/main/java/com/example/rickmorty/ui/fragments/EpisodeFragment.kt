package com.example.rickmorty.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AbsListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rickmorty.R
import com.example.rickmorty.adapters.CharacterViewEpisodeAdapter
import com.example.rickmorty.databinding.FragmentEpisodeBinding
import com.example.rickmorty.models.Episode
import com.example.rickmorty.utils.ConnectionLiveData
import com.example.rickmorty.utils.Constants
import com.example.rickmorty.utils.Resource
import com.example.rickmorty.utils.Resources.showSnackBar
import com.example.rickmorty.viewmodels.CharacterViewModel
import com.example.rickmorty.viewmodels.EpisodeViewModel


class EpisodeFragment : Fragment(R.layout.fragment_episode) {


    private lateinit var binding: FragmentEpisodeBinding
    private lateinit var viewModel: EpisodeViewModel
    private lateinit var episodeAdapter: CharacterViewEpisodeAdapter
    private var whenShouldPaginate: Boolean = true
    private lateinit var connectionLiveData : ConnectionLiveData
    private var TAG = "EpisodeFragment"
    private var episode: List<Episode>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEpisodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectionLiveData = ConnectionLiveData(requireContext())
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.ToolBar)
        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(EpisodeViewModel::class.java)

        hideProgressBar()
        setupRecyclerView()
        connectionLiveData.observe(viewLifecycleOwner){connection ->
            if(connection){
                binding.EPISODES.visibility = View.VISIBLE
                binding.noInternetAnimation.visibility = View.GONE
                loadEpisodes()
            }else{
                binding.EPISODES.visibility = View.GONE
                binding.noInternetAnimation.visibility = View.VISIBLE
            }

        }


        episodeAdapter.setOnItemClickListener {
            val action = EpisodeFragmentDirections.actionEpisodeFragment2ToEpisodeViewFragment(it)
            findNavController().navigate(action)
        }

    }

    private fun loadEpisodes() {
        viewModel.getAllEpisode()
        viewModel.episode.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success -> {
                    hideProgressBar()
                    episode = response.data?.results?.toList()
                    episodeAdapter.differ.submitList(episode)
                    isLastPage = viewModel.currentEpisodePage == 4
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        if(it != ""){
                            showSnackBar(it)
                        }
                    }
                }
                is Resource.Loading -> {
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
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling && whenShouldPaginate
            if(shouldPaginate) {
                viewModel.getAllEpisode()
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

    private fun setupRecyclerView(){
        episodeAdapter = CharacterViewEpisodeAdapter()
        binding.EPISODES.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = episodeAdapter
            addOnScrollListener(scrollListener)
        }
    }

    private fun showProgressBar(){
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar(){
        binding.progressBar.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.episode_menu, menu)
        val search  = menu.findItem(R.id.SearchEpisodes)
        val searchView : androidx.appcompat.widget.SearchView =  search.actionView as androidx.appcompat.widget.SearchView
        search(searchView)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun search(searchView: SearchView) {
        searchView.queryHint = resources.getString(R.string.search_by_name)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                searchQuery(query)
                return false
            }
        })
    }

    private fun searchQuery(query: String?) {
       connectionLiveData.observe(viewLifecycleOwner){ connetion ->
           if(connetion){
               if(query.isNullOrEmpty()){
                   whenShouldPaginate = true
                   loadEpisodes()
               }else{
                   viewModel.searchEpisodes(query)
                   searchState()
                   whenShouldPaginate = false
               }
           }else{
               whenShouldPaginate = true
           }
       }
    }

    private fun searchState() {
        viewModel.searchedEpisode.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success -> {
                    episode = it.data?.results
                    episodeAdapter.differ.submitList(episode)
                }
                is Resource.Error -> {
                    showSnackBar(it.message.toString())
                }
            }
        }
    }
}