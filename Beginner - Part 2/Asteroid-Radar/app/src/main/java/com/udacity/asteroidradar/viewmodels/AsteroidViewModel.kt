package com.udacity.asteroidradar.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import com.udacity.asteroidradar.repository.PictureOfTheDayRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AsteroidViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "AsteroidViewModel"

    private val database = getDatabase(application)

    private val asteroidRepository = AsteroidRepository(database)
    private val pictureOfTheDayRepository = PictureOfTheDayRepository(database)

    val listOfAsteroids = asteroidRepository.asteroids

    val pictureOfTheDay = pictureOfTheDayRepository.pictureOfTheDay

    init {
        viewModelScope.launch {

            // Issues running this with airplane mode or no network
            // Put in a try catch to catch these situations and handle them appropriately
            try {
                asteroidRepository.refreshAsteroids()
                pictureOfTheDayRepository.refreshPictureOfTheDay()
            } catch (e: UnknownHostException) { // Handle no internet
                Log.e(TAG, "Unknown host exception: $e");
            } catch (e: HttpException) { // Handle HTTP errors (like 400s, 500s)
                Log.e(TAG, "Http Exception: $e");
            } catch (e: IOException) { // Handle any other I/O errors
                Log.e(TAG, "IO exception: $e");
            } catch (e: SocketTimeoutException) { // Handle request timeout Exception
                Log.e(TAG, "Socket timeout exception: $e");
            }
        }

    }

    /**
     * Factory for constructing AsteroidViewModel with parameter
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AsteroidViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AsteroidViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}