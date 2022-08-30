package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    // Executes each reminder synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    // Testing ROOM database
    @Test
    fun insertReminder_GetById() = runBlocking {
        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO("title", "description", "location", 1.0,1.0)
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database.
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun insertReminder_VerifyData() = runBlocking {
        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO("REMINDER", "DESCRIPTION", "LOCATION", 2.0, 5.0)
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id
        val loadedReminder = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains expected
        assertThat(loadedReminder as ReminderDTO, notNullValue())
        assertThat(loadedReminder.id, `is`(reminder.id))
        assertThat(loadedReminder.title, `is`(reminder.title))
        assertThat(loadedReminder.description, `is`(reminder.description))
        assertThat(loadedReminder.location, `is`(reminder.location))
        assertThat(loadedReminder.latitude, `is`(reminder.latitude))
        assertThat(loadedReminder.longitude, `is`(reminder.longitude))
    }

    @Test
    fun insertReminders_verifyAllSave() = runBlocking {
        // GIVEN - Insert 4 different reminders.
        val reminderOne = ReminderDTO("Title", "Description", "Place", 4.0, 7.0)
        val reminderTwo = ReminderDTO("Title", "Description", "Place", 1.0, 2.0)
        val reminderThree = ReminderDTO("Title", "Description", "Place", 3.0,6.0)
        val reminderFour = ReminderDTO("Title", "Description", "Place", 3.0,8.0)

        database.reminderDao().saveReminder(reminderOne)
        database.reminderDao().saveReminder(reminderTwo)
        database.reminderDao().saveReminder(reminderThree)
        database.reminderDao().saveReminder(reminderFour)

        // WHEN - Load all reminders from the database.
        val loadedReminders = database.reminderDao().getReminders()

        // THEN - size is correct
        assertThat(loadedReminders.size, `is`(4))
        assertThat(loadedReminders[0].id, `is`(reminderOne.id))
        assertThat(loadedReminders[1].id, `is`(reminderTwo.id))
        assertThat(loadedReminders[2].id, `is`(reminderThree.id))
        assertThat(loadedReminders[3].id, `is`(reminderFour.id))
    }


    @Test
    fun deleteAll_VerifyEmpty() = runBlocking {
        // GIVEN - We insert a reminder into the DAO.
        val originalReminder = ReminderDTO("title", "description", "location", 1.0,1.0)
        database.reminderDao().saveReminder(originalReminder)

        // WHEN - Delete all reminders
        database.reminderDao().deleteAllReminders()

        // THEN - Check that it is empty
        val loaded = database.reminderDao().getReminders()
        assertThat(loaded?.isEmpty(), `is`(true))
    }
}