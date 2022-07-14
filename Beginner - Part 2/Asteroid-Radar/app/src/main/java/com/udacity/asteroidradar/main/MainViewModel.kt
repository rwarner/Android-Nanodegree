package com.udacity.asteroidradar.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


enum class NasaApiStatus { LOADING, ERROR, DONE }

class MainViewModel : ViewModel() {

    // _VARIABLE in the following ViewModel represent the internal variables that are mutable
    // or changeable here in this class
    // The same name, without the "_" is not changeable / immutable is for external accessors


    // Stores the status of the most recent request
    private val _status = MutableLiveData<NasaApiStatus>()
    val status: LiveData<NasaApiStatus>
        get() = _status

    // Stores which Asteroid to navigate to
    private val _navigateToSelectedProperty = MutableLiveData<Asteroid>()
    val navigateToSelectedProperty: LiveData<Asteroid>
        get() = _navigateToSelectedProperty

    // Stores what the picture of the day object is
    private val _pictureOfDay = MutableLiveData<PictureOfDay>()
    val pictureOfDay: LiveData<PictureOfDay>
        get() = _pictureOfDay

    // Stores the list of Asteroids pulled down
    private val _asteroids = MutableLiveData<List<Asteroid>>()
    val asteroids: LiveData<List<Asteroid>>
        get() = _asteroids

    /**
     * On initialization of this ViewModel, execute the following
     */
    init {
        getPictureOfTheDay()
        getAsteroids()
    }

    /**
     * Obtain the Picture Of The Day from the Retrofit API service.
     */
    private fun getPictureOfTheDay() {
        viewModelScope.launch {
            try {
                // Fetch the picture of the day from the NASA API
                // Then set the value of Picture Of the Day ViewModel Object
                // For the layout to listen to
                _pictureOfDay.value = AsteroidApi.retrofitService.getPotd(Constants.API_KEY)
            } catch (e: Exception) {
                Log.e(Constants.LOG_TAG, e.toString());
            }
        }
    }

    /**
     * Obtain the Asteroids from the Retrofit API service.
     */
    private fun getAsteroids() {
        viewModelScope.launch {

            _status.value = NasaApiStatus.LOADING

            try {

                //Get the current date, so we can pass along to the API
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                val currentDate = sdf.format(Date())

                // Obtain the JSON object from the retrofit API service. This in particular API call is dynamic, so
                // we are relying on the parseAsteroidsJsonResult() to get the actual values which Moshi / Scalars
                // can't do on it's own
                var jsonObject = JSONObject(AsteroidApi.retrofitService.getAsteroids(currentDate, Constants.API_KEY))
                _asteroids.value = parseAsteroidsJsonResult(jsonObject)

                _status.value = NasaApiStatus.DONE

            } catch (e: Exception) {
                Log.e(Constants.LOG_TAG, e.toString());
                _status.value = NasaApiStatus.ERROR
            }
        }
    }

    /**
     * Function to set which Asteroid to pass along for navigation
     */
    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedProperty.value = asteroid
    }

    /**
     * Set to false to prevent unwanted extra navigations once we're done
     */
    fun displayAsteroidDetailsComplete() {
        _navigateToSelectedProperty.value = null
    }

}

