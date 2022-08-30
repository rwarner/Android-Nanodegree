package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

// Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        if(shouldReturnError) {
            return Result.Error(
                "Error exception for data"
            )
        }

        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error(
            "Tasks not found"
        )
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        try {
            reminders?.let { return Result.Success( it[id.toInt()] ) }

            if(shouldReturnError) {
                return Result.Error(
                    "Error exception for data"
                )
            }

        } catch (e: Exception) {
            return Result.Error("Error exception for data")
        }


        return Result.Error(
            "Tasks not found"
        )
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}