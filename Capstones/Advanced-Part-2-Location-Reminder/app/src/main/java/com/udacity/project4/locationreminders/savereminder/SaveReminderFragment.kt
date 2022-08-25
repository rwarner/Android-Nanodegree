package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.vmadalin.easypermissions.EasyPermissions
import kotlinx.android.synthetic.main.fragment_save_reminder.*
import kotlinx.android.synthetic.main.fragment_select_location.*
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class SaveReminderFragment : BaseFragment() {

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    companion object {
        private val TAG = "SameReminderFragment"
        internal const val ACTION_GEOFENCE_REMINDER = "ReminderActivity.action.ACTION_GEOFENCE_REMINDER"
    }

    private lateinit var latestReminderData: ReminderDataItem
    private lateinit var geofencingClient: GeofencingClient

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var resolutionForResult: ActivityResultLauncher<IntentSenderRequest>


    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_REMINDER

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        // Setup new permission activity result for inside this fragment
        // for the background location permission request
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

        resolutionForResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK)
                checkDeviceLocationSettingsAndSave()
            else {
                onPermissionsDenied()
            }
        }

        binding.selectLocation.setOnClickListener {

            // Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())

        }
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        binding.saveReminder.setOnClickListener {

            if(reminderTitle.text.isBlank() || reminderDescription.text.isBlank()) {
                Snackbar.make(this.requireView(), "Enter a title and description", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkDeviceLocationSettingsAndSave()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    private fun checkDeviceLocationSettingsAndSave(): Boolean {

        var success = false

        val perms = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if (!EasyPermissions.hasPermissions(context, *perms)) {
            Snackbar.make(
                this.requireView(),
                "Background location required for geofencing", Snackbar.LENGTH_SHORT
            ).show()
            permissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

            return false
        }

        // Check for location itself on the device, non-permissions
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->

            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                resolutionForResult.launch(intentSenderRequest)
            }

            Snackbar.make(
                this.requireView(),
                R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
                checkDeviceLocationSettingsAndSave()
            }.show()
            success = false
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                success = true

                saveGeofence()
            }
        }

        return success
    }

    @SuppressLint("MissingPermission")
    fun saveGeofence() {

        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        // Create new Reminder Data Item
        latestReminderData = ReminderDataItem(
            title,
            description,
            location,
            latitude,
            longitude
        )

        val geofence = Geofence.Builder()
            .setRequestId(latestReminderData.id)
            .setCircularRegion(_viewModel.selectedPOI.value!!.latLng.latitude,
                _viewModel.selectedPOI.value!!.latLng.longitude,
                100f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()


        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                Log.d("Added Geofence: ", latestReminderData.location.toString())
            }
            addOnFailureListener {
                Log.e(TAG, it.message!!)
                Log.e(TAG, "Error trying to save a geofence for: " + latestReminderData.location.toString())
            }
        }

        _viewModel.validateAndSaveReminder(latestReminderData)
    }

    private fun onPermissionsDenied() {
        Snackbar.make(
            this.requireView(),
            "Location not granted, not able to add geofence", Snackbar.LENGTH_INDEFINITE
        ).setAction(android.R.string.ok) {
            checkDeviceLocationSettingsAndSave()
        }.show()
        Log.d(RemindersActivity.TAG, "Background Location permission denied")
    }

    private fun onPermissionsGranted() {
        Log.d(RemindersActivity.TAG, "Background Location permission granted")
        checkDeviceLocationSettingsAndSave()
    }
}
