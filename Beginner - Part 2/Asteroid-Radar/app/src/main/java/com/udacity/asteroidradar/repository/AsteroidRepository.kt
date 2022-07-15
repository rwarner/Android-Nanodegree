/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.network.AsteroidApi
import com.udacity.asteroidradar.network.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.network.NetworkAsteroidContainer
import com.udacity.asteroidradar.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


/**
 * Repository for fetching asteroids from the network and storing them on disk.
 */
class AsteroidRepository(private val database: AsteroidsDatabase) {

    /**
     * List of Asteroids that can be shown on the screen
     */
    val asteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAsteroids()) {
        it.asDomainModel()
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