package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.switchMap
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.network.AsteroidApi
import com.udacity.asteroidradar.network.NetworkAsteroidContainer
import com.udacity.asteroidradar.network.asDatabaseModel
import com.udacity.asteroidradar.network.parseAsteroidsJsonResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


/**
 * Repository for fetching asteroids from the network and storing them on disk.
 */
class AsteroidRepository(private val database: AsteroidsDatabase) {

    private val filterSelection = MutableLiveData<String>()

    /**
     * List of Asteroids that can be shown on the screen
     */
    private val todayAsteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getTodayAsteroids(SimpleDateFormat("yyyy-MM-dd").format(Date()))) {
        it.asDomainModel()
    }
    private val weekAsteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getWeekAsteroids(SimpleDateFormat("yyyy-MM-dd").format(Date()))) {
        it.asDomainModel()
    }
    private val allAsteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAllAsteroids()) {
        it.asDomainModel()
    }


    val filteredAsteroids: LiveData<List<Asteroid>> = filterSelection.switchMap {
        when(it) {
            "today" -> todayAsteroids
            "week" -> weekAsteroids
            "all" -> allAsteroids
            else -> {
                allAsteroids
            }
        }
    }

    fun setFilter(filter: String) {
        filterSelection.value = filter
    }


    /**
     * Refresh the asteroids stored in offline cache
     *
     * To actually load asteroids for use, observe [asteroids]
     */
    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            //Get the current date, so we can pass along to the API
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val currentDate = sdf.format(Date())

            // Obtain the JSON object from the retrofit API service. This in particular API call is dynamic, so
            // we are relying on the parseAsteroidsJsonResult() to get the actual values which Moshi / Scalars
            // can't do on it's own
            var jsonObject = JSONObject(AsteroidApi.retrofitService.getAsteroids(currentDate, Constants.API_KEY))
            val listOfAsteroids = parseAsteroidsJsonResult(jsonObject)

            val networkAsteroid = NetworkAsteroidContainer(listOfAsteroids.toList())

            //  * <- (spread operator) It allows you to pass in an array to a function that expects varargs.
            database.asteroidDao.insertAllAsteroids(*networkAsteroid.asDatabaseModel())
        }
    }
}