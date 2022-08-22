package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
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

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.putExtra("ReminderData", latestReminderData)
        intent.action = ACTION_GEOFENCE_REMINDER
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
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
        binding.selectLocation.setOnClickListener {

            val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            if (!EasyPermissions.hasPermissions(context, *perms)) {
                (activity as RemindersActivity).requestLocationPermission()
            } else {
                _viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
            }
            //            Navigate to another fragment to get the user location
        }
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        binding.saveReminder.setOnClickListener {

            if(reminderTitle.text.isBlank() || reminderDescription.text.isBlank()) {
                Snackbar.make(this.requireView(), "Enter a title and description", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
                .setExpirationDuration(TimeUnit.HOURS.toMillis(1))
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
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
