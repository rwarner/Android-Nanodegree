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

package com.udacity.asteroidradar.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.domain.PictureOfTheDay

/**
 * Asteroid object
 */
@Entity(tableName = "asteroids_table")
data class DatabaseAsteroid constructor(
    @PrimaryKey
    val id: Long,
    val codename: String,
    val closeApproachDate: String,
    val absoluteMagnitude: Double,
    val estimatedDiameter: Double,
    val relativeVelocity: Double,
    val distanceFromEarth: Double,
    val isPotentiallyHazardous: Boolean
)

/**
 * Converts database objects to domain objects
 */
fun List<DatabaseAsteroid>.asDomainModel(): List<Asteroid> {
    return map {
        Asteroid (
            id = it.id,
            codename = it.codename,
            closeApproachDate = it.closeApproachDate,
            absoluteMagnitude = it.absoluteMagnitude,
            estimatedDiameter = it.estimatedDiameter,
            relativeVelocity = it.relativeVelocity,
            distanceFromEarth = it.distanceFromEarth,
            isPotentiallyHazardous = it.isPotentiallyHazardous
        )
    }
}


/**
 * Picture Of The Day Object
 */
@Entity(tableName = "potd_table")
data class DatabasePictureOfTheDay constructor(
    @PrimaryKey
    val media_type: String,
    val title: String,
    val url: String
)

/**
 * Converts database objects to domain objects
 */
@JvmName("asDomainModelDatabasePictureOfTheDay")
fun List<DatabasePictureOfTheDay>.asDomainModel(): PictureOfTheDay? {

    val dbPotd = map {
        PictureOfTheDay (
            media_type = it.media_type,
            title = it.title,
            url = it.url
        )
    }

    if(dbPotd.isNotEmpty()) {
        return dbPotd[0]
    }

    return null

}