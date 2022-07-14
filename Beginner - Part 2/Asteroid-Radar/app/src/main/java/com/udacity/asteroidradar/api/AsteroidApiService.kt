package com.udacity.asteroidradar.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(Constants.BASE_URL)
    .client(OkHttpClient.Builder().apply {
        connectTimeout(1, TimeUnit.MINUTES)
        readTimeout(45, TimeUnit.SECONDS)
        writeTimeout(45, TimeUnit.SECONDS)
    }.build())
    .build()

interface AsteroidApiService {

    // We don't need the "end_date" query, if we omit it will automatically get the next seven days
    @GET("neo/rest/v1/feed")
    suspend fun getAsteroids(@Query("start_date") startDate: String, @Query("api_key") apiKey: String): String

    @GET("planetary/apod")
    suspend fun getPotd(@Query("api_key") apiKey: String): PictureOfDay
}

object AsteroidApi {
    val retrofitService : AsteroidApiService by lazy {
        retrofit.create(AsteroidApiService::class.java)
    }
}

enum class MarsApiFilter(val value: String) { SHOW_RENT("rent"), SHOW_BUY("buy"), SHOW_ALL("all")}


