package com.example.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.CivicsHttpClient
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch

//TODO: Construct ViewModel and provide election datasource
class ElectionsViewModel(
    val database: ElectionDao,
    application: Application
) : AndroidViewModel(application) {


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

    //TODO: Create val and functions to populate live data for upcoming elections from the API and saved elections from local database

    fun fetchUpcomingElections() {
        viewModelScope.launch {
            try {
                _upcomingElections.value = CivicsApi.retrofitService.getElections(CivicsHttpClient.API_KEY).elections
                Log.d("Elections View Model", "Success: " + _upcomingElections.value!!.toList().toString())
            } catch (e: Exception) {
                // Clear recycler view
                Log.e("Elections View Model", e.stackTraceToString())
                _savedElections.value = ArrayList()
            }
        }
    }

    //TODO: Create functions to navigate to saved or upcoming election voter info

}