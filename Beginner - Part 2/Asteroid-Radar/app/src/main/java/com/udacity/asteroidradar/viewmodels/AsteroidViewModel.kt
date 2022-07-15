package com.udacity.asteroidradar.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import com.udacity.asteroidradar.repository.PictureOfTheDayRepository
import kotlinx.coroutines.launch

class AsteroidViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)

    private val asteroidRepository = AsteroidRepository(database)
    private val pictureOfTheDayRepository = PictureOfTheDayRepository(database)

    val listOfAsteroids = asteroidRepository.asteroids

    val pictureOfTheDay = pictureOfTheDayRepository.pictureOfTheDay

    init {
        viewModelScope.launch {
            asteroidRepository.refreshAsteroids()
            pictureOfTheDayRepository.refreshPictureOfTheDay()
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