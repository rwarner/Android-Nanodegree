package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionDetailBinding
import com.example.android.politicalpreparedness.network.models.Division


class ElectionDetailFragment : Fragment() {

    lateinit var electionDetailViewModel: ElectionDetailViewModel
    private lateinit var electionId: String
    private lateinit var division: Division

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentElectionDetailBinding.inflate(inflater)
        binding.lifecycleOwner = this

        // Obtain the election ID and the division from the previous fragment
        electionId = arguments?.get("arg_election_id").toString()
        division = arguments?.get("arg_division") as Division

        val application = requireNotNull(this.activity).application

        // Create an instance of the ViewModel Factory.
        val dataSource = ElectionDatabase.getInstance(application).electionDao
        val viewModelFactory = ElectionDetailViewModelFactory(dataSource, application)

        // Get a reference to the view model associated with this fragment
        electionDetailViewModel = ViewModelProvider(this, viewModelFactory)[ElectionDetailViewModel::class.java]
        binding.electionDetailViewModel = electionDetailViewModel

        electionDetailViewModel.title.observe(viewLifecycleOwner) {
            val header = binding.fragmentElectionDetailToolbarElectionName
            if(it.isNullOrEmpty()) {
                header.title = "Name is missing"
            } else {
                header.title = it
            }
        }

        electionDetailViewModel.date.observe(viewLifecycleOwner) {
            val textView = binding.fragmentElectionDetailTextViewDate
            if(it.isNullOrEmpty()) {
                textView.text = "Date is missing"
            } else {
                textView.text = it
            }
        }
        electionDetailViewModel.ballotInformationURL.observe(viewLifecycleOwner) {
            val textView = binding.fragmentElectionDetailTextViewBallotInfo
            if(it.isBlank()) {
                textView.visibility = View.GONE
            } else {
                textView.visibility = View.VISIBLE

                // Setup the on click listener to bring you to the URL in a browser
                textView.setOnClickListener {
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(electionDetailViewModel.ballotInformationURL.value))
                    startActivity(browserIntent)

                }

                // Underline the text view to show it's clickable
                val content = SpannableString(textView.text)
                content.setSpan(UnderlineSpan(), 0, content.length, 0)
                textView.text = content
            }
        }

        electionDetailViewModel.votingLocationURL.observe(viewLifecycleOwner) {
            val textView = binding.fragmentElectionDetailTextViewVotingLocations
            if(it.isBlank()) {
                textView.visibility = View.GONE
            } else {
                textView.visibility = View.VISIBLE

                // Setup the on click listener to bring you to the URL in a browser
                textView.setOnClickListener {
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(electionDetailViewModel.votingLocationURL.value))
                    startActivity(browserIntent)

                }

                // Underline the text view to show it's clickable
                val content = SpannableString(textView.text)
                content.setSpan(UnderlineSpan(), 0, content.length, 0)
                textView.text = content
            }
        }

        electionDetailViewModel.followingElection.observe(viewLifecycleOwner) {
            val button = binding.fragmentElectionDetailButtonFollowElection
            if(it != null && it) {
                button.text = getString(R.string.fragment_election_info_unfollow_election)
                button.setOnClickListener {
                    electionDetailViewModel.unfollowElection()
                }
            } else {
                button.text = getString(R.string.fragment_election_info_follow_election)
                button.setOnClickListener {
                    electionDetailViewModel.followElection()
                }
            }
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch the upcoming elections from the API when the view is created
        electionDetailViewModel.fetchElectionDetail(electionId, division)

        // Determine if this is an election we follow
        electionDetailViewModel.determineIfWeFollow(electionId)

    }
}