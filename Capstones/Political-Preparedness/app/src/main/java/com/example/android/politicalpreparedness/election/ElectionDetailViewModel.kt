package com.example.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsHttpClient
import com.example.android.politicalpreparedness.network.jsonadapter.ElectionAdapter
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.State
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ElectionDetailViewModel(
    val database: ElectionDao,
    application: Application
) : AndroidViewModel(application) {

    private var _date = MutableLiveData<String>()
    val date: LiveData<String>
        get() = _date

    private var _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title

    private var _followingElection = MutableLiveData<Boolean>()
    val followingElection: LiveData<Boolean>
        get() = _followingElection

    private var _votingLocationURL = MutableLiveData<String>()
    val votingLocationURL: LiveData<String>
        get() = _votingLocationURL

    private var _ballotInformationURL = MutableLiveData<String>()
    val ballotInformationURL: LiveData<String>
        get() = _ballotInformationURL

    private lateinit var currentElection: Election

    /**
     * Grab election information from the getVoterInfo API
     *
     * return the details to the appropriate view model items
     */
    fun fetchElectionDetail(electionId: String, division: Division) {
        viewModelScope.launch {
            try {
                // Create an address to give to the Voter Info API from the given division
                val address = "${division.state}, US"

                // Get all election detail information
                var electionInformation = CivicsApi.retrofitService.getVoterInfo(CivicsHttpClient.API_KEY, electionId, address)//"1263 Pacific Ave. Kansas City, KS")

                // Set the name and date to the view model items
                _title.value = electionInformation.election.name
                _date.value = electionInformation.election.electionDay.toString()

                // Save off the election item for local use
                currentElection = electionInformation.election

                // Set the URL for the ballot information if it exists
                electionInformation.state!![0].electionAdministrationBody.electionInfoUrl.let {
                    if(it == null) {
                        _ballotInformationURL.value = ""
                    } else {
                        _ballotInformationURL.value = it
                    }
                }

                // Set the URL for the voting location information if it exists
                electionInformation.state!![0].electionAdministrationBody.votingLocationFinderUrl.let {
                    if(it == null) {
                        _votingLocationURL.value = ""
                    } else {
                        _votingLocationURL.value = it
                    }
                }

                Log.d("Election Detail View Model", "Success: $electionInformation")
            } catch (e: Exception) {
                Log.e("Election Detail View Model", e.stackTraceToString())
            }
        }
    }

    /**
     * Query the database to see if we already follow this election
     */
    fun determineIfWeFollow(electionId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val validElection = database.getElectionWithId(electionId.toLong())
            if(validElection != null) {
                _followingElection.postValue(true)
            } else {
                _followingElection.postValue(false)
            }
        }
    }

    /**
     * Follow the election by inserting it into the DB
     */
    fun followElection() {
        CoroutineScope(Dispatchers.IO).launch {
            database.insert(currentElection)
            _followingElection.postValue(true)
        }
    }


    /**
     * Unfollow the election by deleting it into the DB
     */
    fun unfollowElection() {
        CoroutineScope(Dispatchers.IO).launch {
            database.delete(currentElection)
            _followingElection.postValue(false)
        }
    }

}