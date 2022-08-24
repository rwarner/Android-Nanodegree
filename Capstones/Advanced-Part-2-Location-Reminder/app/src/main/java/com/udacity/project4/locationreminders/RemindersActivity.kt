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

}
