package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
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
        private val TAG = "Representative Fragment"
        private val REQUEST_LOCATION_PERMISSION = 1
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
            if(checkLocationPermissions()) {
                // Get location
                if(getLocation()) {
                    // Fill in fields with appropriate location information
                    representativeViewModel.fetchRepresentativeList(address.toFormattedString())
                    binding.fragmentRepresentativeEditTextAddressLine1.setText(address.line1)
                    binding.fragmentRepresentativeEditTextAddressLine2.setText(address.line2)

                    // TODO: Set state in spinner

                    binding.fragmentRepresentativeEditTextCity.setText(address.city)
                    binding.fragmentRepresentativeEditTextZip.setText(address.zip)
                    hideKeyboard()
                }
            }
        }

        binding.fragmentRepresentativeButtonFindMyRep.setOnClickListener {

            val address = "1263 Pacific Ave. Kansas City, KS"
            representativeViewModel.fetchRepresentativeList(address)

            // Grab
            hideKeyboard()
        }

        return binding.root
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Check if location permissions are granted and if so enable the
        // use of adding the address automatically
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(requireContext(), "Permission granted, fetching address", Toast.LENGTH_SHORT).show()
                getLocation()
            } else {
                Toast.makeText(requireContext(), "Permission not granted, unable to fetch address", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Initiate checking for location permission if we do no have it
     */
    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            requestForegroundLocationPermissions()
            false
        }
    }

    /**
     * Requests ACCESS_FINE_LOCATION and (on Android 10+ (Q) ACCESS_BACKGROUND_LOCATION.)
     */
    @TargetApi(29)
    private fun requestForegroundLocationPermissions() {
        val resultCode =  REQUEST_LOCATION_PERMISSION

        Log.d(TAG, "Request foreground location permission")
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            resultCode
        )
    }

    /**
     * Check if permission is already granted and return (true = granted, false = denied/other)
     */
    private fun isPermissionGranted() : Boolean {
        val permissionGranted = PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

        Log.d(TAG, "Foreground permissions approved: $permissionGranted")

        return permissionGranted
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(): Boolean {

        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val criteria = Criteria()

        val location: Location? = locationManager!!.getLastKnownLocation(
            locationManager.getBestProvider(
                criteria,
                false
            )!!
        )
        if (location != null) {
            Log.d(TAG, "Lat: ${location.latitude} Long: ${location.longitude}")
            try {
                address = geoCodeLocation(location)
                Log.d(TAG, "Address: ${address.toFormattedString()}")
            } catch (e: Exception) {
                // If this crashes, it most likely is due to being at a location with a facility name
                // which kept happenning
                val geocoder = Geocoder(context, Locale.getDefault())
                address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    .map { address ->
                        Address(address.featureName, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
                    }.first()
                Log.d(TAG, "Address: ${address.toFormattedString()}")

            }
        } else {
            Log.e(TAG, "Location is null")
            return false
        }

        return true
    }


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