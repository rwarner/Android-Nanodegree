package com.udacity.asteroidradar.domain

import com.squareup.moshi.Json
import java.io.Serializable

/**
 * Domain objects are plain Kotlin data classes that represent the things in our app. These are the
 * objects that should be displayed on screen, or manipulated by the app.
 *
 * @see database for objects that are mapped to the database
 * @see network for objects that parse or prepare network calls
 */

data class Asteroid(val id: Long, val codename: String, val closeApproachDate: String,
                    val absoluteMagnitude: Double, val estimatedDiameter: Double,
                    val relativeVelocity: Double, val distanceFromEarth: Double,
                    val isPotentiallyHazardous: Boolean) : Serializable {
}

data class PictureOfTheDay(@Json(name = "media_type") val media_type: String, val title: String,
                           val url: String) {

}