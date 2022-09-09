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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import java.util.*


class RepresentativeFragment : Fragment() {

    companion object {
        private val TAG = "Representative Fragment"
        private val REQUEST_LOCATION_PERMISSION = 1
    }

    lateinit var binding: FragmentRepresentativeBinding
    lateinit var representativeViewModel: RepresentativeViewModel
    var address: Address = Address("", "", "", "", "")

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?{
        binding = FragmentRepresentativeBinding.inflate(inflater)
        binding.lifecycleOwner = this

        val application = requireNotNull(this.activity).application

        // Get a reference to the view model associated
        representativeViewModel = ViewModelProvider(this)[RepresentativeViewModel::class.java]

        // Set the data binding to the view model
        binding.representativeViewModel = representativeViewModel

        // Set the layout manager for the recycler view (REQUIRED to show data)
        binding.fragmentRepresentativeRecyclerViewMyReps.layoutManager = LinearLayoutManager(context)

        val representativeListAdapter = RepresentativeListAdapter(RepresentativeListener { representative ->

        })

        // Setup observers for the data that changes in the view model
        representativeViewModel.listOfRepresentatives.observe(viewLifecycleOwner) {
            representativeListAdapter.submitList(it)
        }

        // Set the adapter for the RecyclerView "My Reps"
        binding.fragmentRepresentativeRecyclerViewMyReps.adapter = representativeListAdapter

        // When clicking the Use My Location button, execute the following
        binding.fragmentRepresentativeButtonUseMyLocation.setOnClickListener {

            // Obtain permission
            if(checkLocationPermissions()) {
                // Get location
                if(getLocation()) {
                    // Set the address in the view model to auto update any fields
                    representativeViewModel.setAddress(address)

                    // Fetch reps automatically
                    representativeViewModel.fetchRepresentativeList(address.toFormattedString())
                    hideKeyboard()
                }
            }
        }

        // When clicking the Find My Rep button, execute the following
        binding.fragmentRepresentativeButtonFindMyRep.setOnClickListener {

            val address = StringBuffer()

            // Two way data binding was overly complicated when this gets the job done
            address.append(binding.fragmentRepresentativeEditTextAddressLine1.text)
            address.append(" ")
            address.append(binding.fragmentRepresentativeEditTextAddressLine2.text)
            address.append(", ")
            address.append(binding.fragmentRepresentativeEditTextCity.text)
            address.append(", ")
            address.append(binding.fragmentRepresentativeEditTextState.selectedItem.toString())
            address.append(" ")
            address.append(binding.fragmentRepresentativeEditTextZip.text)

            representativeViewModel.fetchRepresentativeList(address.toString())

            // Grab
            hideKeyboard()
        }

        // Set all address fields
        setupFieldsForAddress()

        return binding.root
    }

    /**
     * When the address changes in the [RepresentativeViewModel], update the fields in the fragment view
     */
    private fun setupFieldsForAddress() {
        representativeViewModel.address.observe(viewLifecycleOwner) {

            binding.fragmentRepresentativeEditTextAddressLine1.setText(it.line1)
            binding.fragmentRepresentativeEditTextAddressLine2.setText(it.line2)

            // Set the spinner value
            binding.fragmentRepresentativeEditTextState.setSelection(
                (binding.fragmentRepresentativeEditTextState.adapter as ArrayAdapter<String?>).getPosition(
                    it.state
                )
            )

            binding.fragmentRepresentativeEditTextCity.setText(it.city)
            binding.fragmentRepresentativeEditTextZip.setText(it.zip)

        }
    }


    /**
     * Hide keyboard when needed programmatically
     */
    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }


    /**
     * Get [Address] object for a given [Location] object
     */
    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
            .map { address ->
                Address(address.thoroughfare, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
            }
            .first()
    }

    /****** Permission methods below ******/

    /**
     * Respond appropriately to permissions checks
     */
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
     * Initiate checking for location permission if we do not have it
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

    /**
     * Obtain Lat/Long / Address for given Location from Location services
     */
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
                // which kept happening
                val geocoder = Geocoder(context, Locale.getDefault())
                address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    .map { address ->
                        Address(address.featureName, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
                    }.first()
                Log.d(TAG, "Address: ${address.toFormattedString()}")

            }
        } else {
            val errorMsg = "Location is null"

            Snackbar.make(
                requireView(),
                errorMsg,
                Snackbar.LENGTH_SHORT
            ).show()

            Log.e(TAG, errorMsg)
            return false
        }

        return true
    }


}