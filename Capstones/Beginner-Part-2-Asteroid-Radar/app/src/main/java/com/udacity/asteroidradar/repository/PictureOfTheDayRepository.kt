package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.domain.PictureOfTheDay
import com.udacity.asteroidradar.network.AsteroidApi
import com.udacity.asteroidradar.network.NetworkPictureOfTheDayContainer
import com.udacity.asteroidradar.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Repository for fetching Picture Of The Day from the network and storing them on disk.
 */
class PictureOfTheDayRepository(private val database: AsteroidsDatabase) {

    /**
     * List of Picture Of The Day that can be shown on the screen
     */
    val pictureOfTheDay: LiveData<PictureOfTheDay> = Transformations.map(database.potdDao.getAllPictureOfTheDay()) {
        it.asDomainModel()
    }


    /**
     * Refresh the Picture Of The Day stored in offline cache
     *
     * To actually load Picture Of The Day for use, observe [asteroids]
     */
    suspend fun refreshPictureOfTheDay() {
        withContext(Dispatchers.IO) {

            val networkPictureOfTheDay = NetworkPictureOfTheDayContainer(AsteroidApi.retrofitService.getPotd(Constants.API_KEY))

            database.potdDao.insertPotd(networkPictureOfTheDay.asDatabaseModel())
        }
    }
}