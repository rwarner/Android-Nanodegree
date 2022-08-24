package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import kotlinx.android.synthetic.main.fragment_select_location.*
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        private var TAG = "SelectionLocationFragment"
    }
    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private lateinit var lastSelected: PointOfInterest

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setDisplayHomeAsUpEnabled(true)

        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.map_options, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.normal_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_NORMAL
                        true
                    }
                    R.id.hybrid_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_HYBRID
                        true
                    }
                    R.id.satellite_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                        true
                    }
                    R.id.terrain_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)


        // Setup new permission activity result for inside this fragment
        // for the fine location permission request
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onPermissionsGranted()
            }
            else {
                onPermissionsDenied()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saveButton.setOnClickListener {
            onLocationSelected()
        }
    }

    private fun onLocationSelected() {

        val snippet = String.format(Locale.getDefault(), getString(R.string.lat_long_snippet), lastSelected.latLng.latitude, lastSelected.latLng.longitude)

        _viewModel.reminderSelectedLocationStr.value = lastSelected.name
        _viewModel.selectedPOI.value = lastSelected //PointOfInterest(lastSelected.latLng, snippet, lastSelected.name)
        _viewModel.latitude.value = lastSelected.latLng.latitude
        _viewModel.longitude.value = lastSelected.latLng.longitude

        findNavController().popBackStack()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap

        setMapStyle(map)
        setPoiClick(map)

        getLocationForMap()


    }

    @SuppressLint("MissingPermission")
    private fun getLocationForMap() {

        val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (!EasyPermissions.hasPermissions(context, *perms)) {
            Snackbar.make(
                this.requireView(),
                "Fine location required for displaying location on map", Snackbar.LENGTH_SHORT
            ).show()
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true

        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val criteria = Criteria()

        val location: Location? = locationManager!!.getLastKnownLocation(
            locationManager.getBestProvider(
                criteria,
                false
            )!!
        )
        if (location != null) {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.getLatitude(),
                        location.getLongitude()
                    ), 13f
                )
            )
        }
    }

    private fun setPoiClick(map: GoogleMap) {

        map.setOnMapClickListener { poi ->
            map.clear()

            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                poi.latitude,
                poi.longitude
            )

            lastSelected = PointOfInterest(
                LatLng(poi.latitude, poi.longitude),
                "",
                snippet
            )

            Log.d(TAG, snippet)

            map.addMarker(
                MarkerOptions()
                    .position(poi)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet))


            saveButton.visibility = View.VISIBLE
        }


        map.setOnPoiClickListener { poi ->
            map.clear()

            lastSelected = poi

            map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )

            saveButton.visibility = View.VISIBLE

        }

    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun onPermissionsDenied() {
        Snackbar.make(
            this.requireView(),
            "Location not able to be used", Snackbar.LENGTH_INDEFINITE
        ).setAction(android.R.string.ok) {
            getLocationForMap()
        }.show()
        Log.d(RemindersActivity.TAG, "Fine Location permission denied")
    }

    private fun onPermissionsGranted() {
        Log.d(RemindersActivity.TAG, "Fine Location permission granted")
        getLocationForMap()
    }


}
