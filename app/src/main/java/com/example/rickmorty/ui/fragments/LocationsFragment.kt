package com.example.rickmorty.ui.fragments

import android.os.Bundle
import android.util.Log
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
import com.example.rickmorty.adapters.LocationsAdapter
import com.example.rickmorty.databinding.FragmentLocationsBinding
import com.example.rickmorty.models.Location
import com.example.rickmorty.models.SingleLocation
import com.example.rickmorty.utils.ConnectionLiveData
import com.example.rickmorty.utils.Constants
import com.example.rickmorty.utils.Resource
import com.example.rickmorty.utils.Resources.showSnackBar
import com.example.rickmorty.viewmodels.LocationViewModel


class LocationsFragment : Fragment(R.layout.fragment_locations) {

    private lateinit var binding: FragmentLocationsBinding
    private lateinit var viewModel: LocationViewModel
    private lateinit var locationAdapter : LocationsAdapter
    private var whenShouldPaginate: Boolean = true
    private lateinit var connectionLiveData: ConnectionLiveData
    private var locations: List<SingleLocation>? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding =  FragmentLocationsBinding.bind(view)
        connectionLiveData = ConnectionLiveData(requireContext())
        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.
            AndroidViewModelFactory.
            getInstance(requireActivity().application))
            .get(LocationViewModel::class.java)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.ToolBar)
        setHasOptionsMenu(true)
        viewModel.currentLocationPage = 1

        binding.progressBar.visibility = View.GONE
        setupRecyclerView()
        connectionLiveData.observe(viewLifecycleOwner) { connection ->
            if (connection) {
                binding.locationsRV.visibility = View.VISIBLE
                binding.noInternetAnimation.visibility = View.GONE
                loadLocations()
            } else {
                binding.locationsRV.visibility = View.GONE
                binding.noInternetAnimation.visibility = View.VISIBLE
            }
        }

        locationAdapter.setOnItemClickListener { location ->
            val action = LocationsFragmentDirections.actionLocationsFragment2ToLocationViewFragment(location)
            findNavController().navigate(action)
        }
    }

    private fun loadLocations(){
        viewModel.getAllLocations()
        viewModel.locations.observe(viewLifecycleOwner){response ->
            when(response){
                is Resource.Success -> {
                    response.data?.let {
                        binding.progressBar.visibility = View.GONE
                        isLoading = false
                        locations = it.results.toList()
                        locationAdapter.differ.submitList(locations)
                        isLastPage = viewModel.currentLocationPage >= viewModel.locationPage!! +1
                    }
                }
                is Resource.Error -> {
                    response.message?.let { message->
                        isLoading = false
                        binding.progressBar.visibility = View.GONE
                        if(message != ""){
                            showSnackBar(message)
                        }
                    }
                }
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    isLoading = true
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
                viewModel.getAllLocations()
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
        locationAdapter = LocationsAdapter()
        binding.locationsRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = locationAdapter
            addOnScrollListener(scrollListener)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.location_menu, menu)
        val search = menu.findItem(R.id.SearchLocations)
        val searchView = search.actionView as SearchView
        setupSearch(searchView)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setupSearch(searchView: SearchView) {
        searchView.queryHint = resources.getString(R.string.search_by_name)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchQuery = newText.toString()
                connectionLiveData.observe(viewLifecycleOwner){ connection ->
                    if(connection){
                        if(searchQuery.isNotEmpty()){
                            whenShouldPaginate = false
                            viewModel.searchByName(searchQuery)
                            searchState()
                        }else{
                            whenShouldPaginate = true
                            loadLocations()
                        }
                    }else{
                        whenShouldPaginate = true
                    }
                }
                return false
            }
        })
    }

    private fun searchState() {
        viewModel.searchedLocations.apply {
            observe(viewLifecycleOwner){ resource ->
                when(resource){
                  is Resource.Success -> {
                      resource.data?.let {
                          locations = it.results
                          locationAdapter.differ.submitList(locations)
                      }
                  }
                  is Resource.Error -> {
                      resource.message?.let {
                          loadLocations()
                      }
                  }
                }
            }
        }
    }

}