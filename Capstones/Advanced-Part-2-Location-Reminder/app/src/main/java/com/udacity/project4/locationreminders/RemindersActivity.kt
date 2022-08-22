package com.udacity.project4.locationreminders

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.udacity.project4.R
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import kotlinx.android.synthetic.main.activity_reminders.*
import kotlinx.android.synthetic.main.fragment_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ReminderActivity"
        private const val PERMISSIONS_REQUEST_LOCATION = 1
        private const val PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
        refreshLayout.isEnabled = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @AfterPermissionGranted(PERMISSIONS_REQUEST_LOCATION)
    fun requestLocationPermission() {
        val perms = arrayOf(ACCESS_FINE_LOCATION)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            Log.d(TAG, "Fine Location permission granted")
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Please grant the FINE location permission to access the Map and display your location",
                PERMISSIONS_REQUEST_LOCATION,
                *perms
            )
        }
    }

    @SuppressLint("InlinedApi")
    @AfterPermissionGranted(PERMISSIONS_REQUEST_BACKGROUND_LOCATION)
    fun requestBackgroundPermission() {
        val perms = arrayOf(ACCESS_BACKGROUND_LOCATION)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            Log.d(TAG, "Background Location permission granted")
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Please grant the (Allow all the time) BACKGROUND location permission, for geofencing features",
                PERMISSIONS_REQUEST_BACKGROUND_LOCATION,
                *perms
            )
        }
    }

    /*
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            PERMISSIONS_REQUEST_LOCATION
        )
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                PERMISSIONS_REQUEST_BACKGROUND_LOCATION
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission granted
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Check background location permission next
                        checkBackgroundLocation()
                    }
                } else {

                    // Permission denied
                    Toast.makeText(this, "Location permission denied, disabling function", Toast.LENGTH_LONG).show()

                    // Check if we are in a state where the user has denied the permission and
                    // selected Don't ask again
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        Log.e(TAG, "Permission denied")
                        // Disable add reminder button
                        findViewById<Button>(R.id.addReminderFAB).visibility = View.GONE
                    }
                }
                return
            }
            PERMISSIONS_REQUEST_BACKGROUND_LOCATION -> {
                // If request is cancelled, the result arrays are empty
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission granted
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d(TAG, "Permissions granted")
                    }
                } else {

                    Log.e(TAG, "Permission denied")

                    // Disable add reminder button
                    findViewById<Button>(R.id.addReminderFAB).visibility = View.GONE
                }
                return

            }
        }
    }*/


}
