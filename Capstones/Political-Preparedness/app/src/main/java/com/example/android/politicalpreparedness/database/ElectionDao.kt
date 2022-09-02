package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.politicalpreparedness.network.models.Election
import retrofit2.http.DELETE

@Dao
interface ElectionDao {

    /**
     * Inserts one election
     */
    @Insert
    suspend fun insert(election: Election)

    /**
     * Selects and returns all elections
     */
    @Query("SELECT * FROM election_table")
    fun getAllElections(): LiveData<List<Election>>

    /**
     * Selects and returns the election for a given ID
     */
    @Query("SELECT * from election_table WHERE id = :key")
    fun getElectionWithId(key: Long): LiveData<Election>

    /**
     * Delete a given election
     */
    @Delete
    fun delete(election: Election)

    /**
     * Deletes all values from the table.
     *
     * This does not delete the table, only its contents.
     */
    @Query("DELETE FROM election_table")
    suspend fun clear()

}