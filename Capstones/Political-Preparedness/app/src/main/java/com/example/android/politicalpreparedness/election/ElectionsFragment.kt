package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide.init
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.databinding.FragmentLaunchBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment: Fragment() {

    lateinit var electionViewModel: ElectionsViewModel
    lateinit var upcomingAdapter: ElectionListAdapter

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

        //TODO: Add ViewModel values and create ViewModel

        //TODO: Add binding values

        //TODO: Link elections to voter info

        val savedAdapter = ElectionListAdapter(ElectionListener { election ->
            electionViewModel.onSavedElectionClicked(election)
        })
        electionViewModel.savedElections.observe(viewLifecycleOwner) {
            savedAdapter.submitList(it)
        }
        binding.fragmentElectionsRecyclerSaved.adapter = savedAdapter

        upcomingAdapter = ElectionListAdapter(ElectionListener { election ->
            electionViewModel.onUpcomingElectionClicked(election)
        })
        electionViewModel.upcomingElections.observe(viewLifecycleOwner) {
            for(item in it) {
                Log.d("TAG", item.name)
            }
            upcomingAdapter.submitList(it)
        }
        binding.fragmentElectionsRecyclerUpcoming.adapter = upcomingAdapter


        //TODO: Populate recycler adapters

        return binding.root
    }


    //TODO: Refresh adapters when fragment loads
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        electionViewModel.fetchUpcomingElections()
    }

}