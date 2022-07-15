package com.udacity.asteroidradar.network

import com.squareup.moshi.JsonClass
import com.udacity.asteroidradar.database.DatabaseAsteroid
import com.udacity.asteroidradar.database.DatabasePictureOfTheDay

/**
 * DataTransferObjects go in this file. These are responsible for parsing responses from the server
 * or formatting objects to send to the server. You should convert these to domain objects before
 * using them.
 */

/**
 * AsteroidHolder holds a list of Asteroids.
 *
 * This is to parse first level of our network result
 */
@JsonClass(generateAdapter = true)
data class NetworkAsteroidContainer(val asteroids: List<NetworkAsteroid>)

@JsonClass(generateAdapter = true)
data class NetworkAsteroid(
        val id: Long,
        val codename: String,
        val closeApproachDate: String,
        val absoluteMagnitude: Double,
        val estimatedDiameter: Double,
        val relativeVelocity: Double,
        val distanceFromEarth: Double,
        val isPotentiallyHazardous: Boolean)

/**
 * Converts data transfer objects to database objects
 */
fun NetworkAsteroidContainer.asDatabaseModel(): Array<DatabaseAsteroid> {
    return asteroids.map {
        DatabaseAsteroid (
            id = it.id,
            codename = it.codename,
            closeApproachDate = it.closeApproachDate,
            absoluteMagnitude = it.absoluteMagnitude,
            estimatedDiameter = it.estimatedDiameter,
            relativeVelocity = it.relativeVelocity,
            distanceFromEarth = it.distanceFromEarth,
            isPotentiallyHazardous = it.isPotentiallyHazardous
        )
    }.toTypedArray()
}


/**
 * This is to parse first level of our network result
 */
@JsonClass(generateAdapter = true)
data class NetworkPictureOfTheDayContainer(val pictureOfTheDay: NetworkPictureOfTheDay)

/**
 * Picture Of The Day Object from the Network
 */
@JsonClass(generateAdapter = true)
data class NetworkPictureOfTheDay constructor(
    val media_type: String,
    val title: String,
    val url: String
)


/**
 * Converts data transfer objects to database objects
 */
fun NetworkPictureOfTheDayContainer.asDatabaseModel(): DatabasePictureOfTheDay {
    return DatabasePictureOfTheDay (
        pictureOfTheDay.media_type,
        pictureOfTheDay.title,
        pictureOfTheDay.url,
    )
}