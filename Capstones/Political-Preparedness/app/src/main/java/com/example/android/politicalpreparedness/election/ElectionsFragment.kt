package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment: Fragment() {

    lateinit var electionViewModel: ElectionsViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = FragmentElectionBinding.inflate(inflater)
        binding.lifecycleOwner = this

        val application = requireNotNull(this.activity).application

        // Create an instance of the ViewModel Factory.
        val dataSource = ElectionDatabase.getInstance(application).electionDao
        val viewModelFactory = ElectionsViewModelFactory(dataSource, application)

        // Get a reference to the ViewModel associated with this fragment.
        electionViewModel =
            ViewModelProvider(this, viewModelFactory)[ElectionsViewModel::class.java]

        binding.electionViewModel = electionViewModel

        // Set the layout manager for the recycler view (REQUIRED to show data)
        binding.fragmentElectionsRecyclerSaved.layoutManager = LinearLayoutManager(context);
        binding.fragmentElectionsRecyclerUpcoming.layoutManager = LinearLayoutManager(context);

        // Initialize the adapters
        val savedAdapter = ElectionListAdapter(ElectionListener { election ->
            electionViewModel.onSavedElectionClicked(election)
        })
        val upcomingAdapter = ElectionListAdapter(ElectionListener { election ->
            electionViewModel.onUpcomingElectionClicked(election)
        })

        // Setup observers for the data that changes in the view model
        electionViewModel.savedElections.observe(viewLifecycleOwner) {
            savedAdapter.submitList(it)
        }
        electionViewModel.upcomingElections.observe(viewLifecycleOwner) {
            upcomingAdapter.submitList(it)
        }

        // Setup the adapters for reach recycler view
        binding.fragmentElectionsRecyclerSaved.adapter = savedAdapter
        binding.fragmentElectionsRecyclerUpcoming.adapter = upcomingAdapter


        // Setup each item that is clicked to navigate to the Election Detail Fragment
        electionViewModel.navigateToUpcomingElections.observe(viewLifecycleOwner) { election ->
            election?.let {
                this.findNavController().navigate(
                    ElectionsFragmentDirections.actionElectionsFragmentToElectionDetailFragment(
                        election.id,
                        election.division
                    )
                )
                electionViewModel.onUpcomingElectionNavigated()
            }
        }

        // Setup each item that is clicked to navigate to the Election Detail Fragment
        electionViewModel.navigateToSavedElections.observe(viewLifecycleOwner) { election ->
            election?.let {
                this.findNavController().navigate(
                    ElectionsFragmentDirections.actionElectionsFragmentToElectionDetailFragment(
                        election.id,
                        election.division
                    )
                )
                electionViewModel.onSavedElectionNavigated()
            }
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch the upcoming elections from the API when the view is created
        electionViewModel.fetchUpcomingElections()

        // Fetch the current saved elections in the database
        electionViewModel.fetchSavedElections()
    }

}