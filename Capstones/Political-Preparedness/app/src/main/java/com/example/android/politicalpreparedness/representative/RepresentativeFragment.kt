package com.example.android.politicalpreparedness.representative

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListener
import java.util.Locale

class RepresentativeFragment : Fragment() {

    companion object {
        //TODO: Add Constant for Location request
    }

    lateinit var representativeViewModel: RepresentativeViewModel
    var address: Address = Address("", "", "", "", "")

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?{
        val binding = FragmentRepresentativeBinding.inflate(inflater)
        binding.lifecycleOwner = this

        val application = requireNotNull(this.activity).application

        // Get a reference to the view model associated
        representativeViewModel = ViewModelProvider(this)[RepresentativeViewModel::class.java]

        // Set the data binding to the view model
        binding.representativeViewModel = representativeViewModel
        binding.address = address

        // Set the layout manager for the recycler view (REQUIRED to show data)
        binding.fragmentRepresentativeRecyclerViewMyReps.layoutManager = LinearLayoutManager(context)

        val representativeListAdapter = RepresentativeListAdapter(RepresentativeListener { representative ->

        })

        // Setup observers for the data that changes in the view model
        representativeViewModel.listOfRepresentatives.observe(viewLifecycleOwner) {
            representativeListAdapter.submitList(it)
        }

        binding.fragmentRepresentativeRecyclerViewMyReps.adapter = representativeListAdapter

        binding.fragmentRepresentativeButtonUseMyLocation.setOnClickListener {

            // Obtain permission

            // Get location

            // Get address for given location
//            var address = geoCodeLocation()

            // Fill in fields with appropriate location information

        }

        binding.fragmentRepresentativeButtonFindMyRep.setOnClickListener {

            val address = "1263 Pacific Ave. Kansas City, KS"
            representativeViewModel.fetchRepresentativeList(address)

            // Grab
            hideKeyboard()
        }

        return binding.root
    }

//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        //TODO: Handle location permission result to get location on permission granted
//    }
//
//    private fun checkLocationPermissions(): Boolean {
//        return if (isPermissionGranted()) {
//            true
//        } else {
//            //TODO: Request Location permissions
//            false
//        }
//    }
//
//    private fun isPermissionGranted() : Boolean {
//        //TODO: Check if permission is already granted and return (true = granted, false = denied/other)
//    }
//
//    private fun getLocation() {
//        //TODO: Get location from LocationServices
//        //TODO: The geoCodeLocation method is a helper function to change the lat/long location to a human readable street address
//    }
//
    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
                .map { address ->
                    Address(address.thoroughfare, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
                }
                .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

}