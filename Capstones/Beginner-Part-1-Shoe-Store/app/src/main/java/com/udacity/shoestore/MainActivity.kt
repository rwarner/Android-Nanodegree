package com.udacity.shoestore

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.udacity.shoestore.databinding.ActivityMainBinding
import com.udacity.shoestore.viewmodels.ShoeListViewModel
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var viewModel : ShoeListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Specifically using DataBinding for LiveData
        val binding : ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Setup action bar with toolbar
        // https://knowledge.udacity.com/questions/695602
        setSupportActionBar(binding.toolbar)

        // Obtain the Nav Host Fragment to utilize
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        // Setup graph from the navigation xml file in the host fragment
        appBarConfiguration = AppBarConfiguration(navController.graph)

        // If we aren't starting at the shoeListFragment this seems to be
        // required to avoid having the navigate up button from appearing in the
        // Shoe List fragment. If these aren't declared, we for some reason,
        // go back to the last fragment DESPITE the popUpTo and popUpToInclusive
        // being declared for Login Fragment and true to knock off the back stack
        // https://stackoverflow.com/questions/61733812/android-navigation-removing-action-bar-back-button-when-popping-back-stack
        // Doesn't work: https://stackoverflow.com/a/51974492/638220

        // Setting top level destinations fixes the navigate up issue
        // This is mainly required for AppBarConfiguration since
        // it messes / mixes up functionality between the back button
        // and the toolbar up button
        val topLevelDestinations = mutableSetOf<Int>()
        topLevelDestinations.add(R.id.loginFragment)
        topLevelDestinations.add(R.id.shoeListFragment)
        val appBarConfiguration = AppBarConfiguration
            .Builder(topLevelDestinations).build()

        setupActionBarWithNavController(navController, appBarConfiguration)

        Timber.plant(Timber.DebugTree())

        viewModel = ViewModelProvider(this).get(ShoeListViewModel::class.java)
        viewModel.setShoes()


    }


    override fun onSupportNavigateUp(): Boolean {
        // REQUIRED to use the back button in AppBar correctly
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    }
}
