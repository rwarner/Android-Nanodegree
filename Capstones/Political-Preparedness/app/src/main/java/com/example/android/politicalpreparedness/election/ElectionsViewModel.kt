package com.example.android.politicalpreparedness.election

import android.app.Application
import android.system.Os.remove
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.CivicsHttpClient
import com.example.android.politicalpreparedness.network.models.Election
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

class ElectionsViewModel(
    val database: ElectionDao,
    application: Application
) : AndroidViewModel(application) {

    private val TAG = "Elections View Model"

    private var _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>>
        get() = _upcomingElections

    private var _savedElections = MutableLiveData<List<Election>>()
    val savedElections: LiveData<List<Election>>
        get() = _savedElections

    /**
     * Variable that tells the Fragment to navigate to a specific [SavedElectionsFragment]
     *
     * This is private because we don't want to expose setting this value to the Fragment.
     */
    private val _navigateToSavedElections = MutableLiveData<Election>()
    /**
     * If this is non-null, immediately navigate to [SavedElectionsFragment] and call [doneNavigating]
     */
    val navigateToSavedElections: LiveData<Election>
        get() = _navigateToSavedElections
    fun onSavedElectionClicked(election: Election) {
        _navigateToSavedElections.value = election
    }
    fun onSavedElectionNavigated() {
        _navigateToSavedElections.value = null
    }


    /**
     * Variable that tells the Fragment to navigate to a specific [UpcomingElectionsFragment]
     *
     * This is private because we don't want to expose setting this value to the Fragment.
     */
    private val _navigateToUpcomingElections = MutableLiveData<Election>()
    /**
     * If this is non-null, immediately navigate to [UpcomingElectionsFragment] and call [doneNavigating]
     */
    val navigateToUpcomingElections: LiveData<Election>
        get() = _navigateToUpcomingElections

    fun onUpcomingElectionClicked(election: Election) {
        _navigateToUpcomingElections.value = election
    }
    fun onUpcomingElectionNavigated() {
        _navigateToUpcomingElections.value = null
    }

    fun fetchUpcomingElections() {
        viewModelScope.launch {
            try {
                var electionsReturned = CivicsApi.retrofitService.getElections(CivicsHttpClient.API_KEY).elections

                // Remove the Test elections
                for(election in electionsReturned) {
                    if(election.name.contains("Test")) {
                        electionsReturned
                        electionsReturned = electionsReturned.toMutableList().apply {
                            remove(election)
                        }.toList()
                    }
                }

                _upcomingElections.value = electionsReturned
                Log.d("Elections View Model (Upcoming)", "Success: " + _upcomingElections.value!!.toList().toString())

            } catch (e: SocketTimeoutException) {
                Log.e("Elections View Model (Upcoming)", e.stackTraceToString())
                Snackbar.make(
                    this@ElectionsViewModel.getApplication(),
                    "Network timeout",
                    Snackbar.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                // Clear recycler view
                Log.e("Elections View Model (Upcoming)", e.stackTraceToString())
                _upcomingElections.value = ArrayList()
            }
        }
    }

    fun fetchSavedElections() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _savedElections.postValue(database.getAllElections())

            } catch (e: Exception) {
                // Clear recycler view
                Log.e("Elections View Model (Saved)", e.stackTraceToString())
                _savedElections.value = ArrayList()
            }
        }
    }
}