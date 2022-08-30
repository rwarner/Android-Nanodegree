package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsNull.isNotNull
import org.hamcrest.core.IsNull.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.isNotNull
import org.mockito.ArgumentMatchers.isNull


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var localRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        localRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_verifyMatching() = runBlocking {
        // GIVEN - Insert one reminder
        val reminder = ReminderDTO("TITLE1", "DESCRIPTION", "Location", 1.0, 1.0)
        localRepository.saveReminder(reminder)

        // WHEN - Get reminder by id
        val result = localRepository.getReminder(reminder.id)

        assertThat(result, not(nullValue()))
        result as Result.Success
        assertThat(result.data.id, `is`(reminder.id))
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.location, `is`(reminder.location))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveReminder_verifyAllReminders() = runBlocking {
        // GIVEN - Insert 4 reminders
        val reminder1 = ReminderDTO("TITLE1", "DESCRIPTION", "Location", 1.0, 1.0)
        val reminder2 = ReminderDTO("TITLE2", "DESCRIPTION", "Location", 1.0, 1.0)
        val reminder3 = ReminderDTO("TITLE3", "DESCRIPTION", "Location", 1.0, 1.0)
        val reminder4 = ReminderDTO("TITLE4", "DESCRIPTION", "Location", 1.0, 1.0)
        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)
        localRepository.saveReminder(reminder3)
        localRepository.saveReminder(reminder4)

        // WHEN load all reminders
        val result = localRepository.getReminders()

        // THEN check reminders
        result as Result.Success
        assertThat(result.data.size, `is`(4))
        assertThat(result.data[0].id, `is`(reminder1.id))
        assertThat(result.data[1].id, `is`(reminder2.id))
        assertThat(result.data[2].id, `is`(reminder3.id))
    }

    @Test
    fun saveReminders_deleteAllAndVerifyEmpty() = runBlocking {
        // GIVEN - Insert reminders
        val reminder1 = ReminderDTO("TITLE1", "DESCRIPTION", "Location", 1.0, 1.0)
        val reminder2 = ReminderDTO("TITLE2", "DESCRIPTION", "Location", 1.0, 1.0)
        val reminder3 = ReminderDTO("TITLE3", "DESCRIPTION", "Location", 1.0, 1.0)
        val reminder4 = ReminderDTO("TITLE4", "DESCRIPTION", "Location", 1.0, 1.0)

        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)
        localRepository.saveReminder(reminder3)
        localRepository.saveReminder(reminder4)

        // Load all
        val result1 = localRepository.getReminders() as Result.Success
        assertThat(result1.data.size, `is`(4))

        // Delete all
        localRepository.deleteAllReminders()

        // Verify all items deleted
        val result2 = localRepository.getReminders() as Result.Success
        assertThat(result2.data.size, `is`(0))
    }

    @Test
    fun getReminder_noDataFoundVerify() = runBlocking {
        // GIVEN - Add one reminder
        val reminder1 = ReminderDTO("TITLE1", "DESCRIPTION", "Location", 1.0, 1.0)
        localRepository.saveReminder(reminder1)

        // THEN - Get a reminder that doesn't exist and verify not found
        val noResult = localRepository.getReminder("1")
        assertThat((noResult as Result.Error).message, `is`("Reminder not found!"))
    }
}