package com.example.android.politicalpreparedness.representative

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsHttpClient
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.network.models.Office
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel: ViewModel() {


    private val TAG = "Elections View Model"

    private var _listOfRepresentatives = MutableLiveData<List<Representative>>()
    val listOfRepresentatives: LiveData<List<Representative>>
        get() = _listOfRepresentatives


    private var _listOfOffices = MutableLiveData<List<Office>>()
    val listOfOffices: LiveData<List<Office>>
        get() = _listOfOffices

    private var _address = MutableLiveData<Address>()
    val address: LiveData<Address>
        get() = _address

        fun fetchRepresentativeList(address: String) {

        viewModelScope.launch {
            val representativeResponse = CivicsApi.retrofitService.getRepresentatives(CivicsHttpClient.API_KEY, address)

            // The following code will prove helpful in constructing a representative from the API.
            // This code combines the two nodes of the RepresentativeResponse into a single official
            val (offices, officials) = representativeResponse
            _listOfRepresentatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }
            _listOfOffices.value = representativeResponse.offices

        }
    }

    //TODO: Create function get address from geo location
    fun getAddressFromGeoLocation(location: Location) {

    }

    //TODO: Create function to get address from individual fields
    fun createAddressFromFields() {

    }
}
