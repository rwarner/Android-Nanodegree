package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import org.koin.test.get
import org.koin.test.inject


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {

    private lateinit var repository: ReminderDataSource

    private lateinit var viewModel: SaveReminderViewModel

    private lateinit var appContext: Application

    @Rule @JvmField
    var activityRule = ActivityTestRule(RemindersActivity::class.java)


    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun setup() {
        stopKoin()

        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }

            // ******* The cast says it's unneeded, but it actually is needed *******
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(getApplicationContext()) }
            single { ReminderListFragment }

        }

        startKoin {
            modules(listOf(myModule))
        }


        //Get our real repository
        viewModel = get()
        repository = get() as ReminderDataSource

    }

    @After
    fun cleanUp() {
//        database.close()
    }


    @Test
    fun showToast() {

        // WHEN - ReminderListFragment launched to display Reminder with empty reminder
        val message = "Test Toast"
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment { it.testShowToast(message) }

        // THEN
        // Check Toast is correct
        onView(withText("Test Toast")).inRoot(
            withDecorView(
                not(
                    `is`(
                        activityRule.activity?.window?.decorView
                    )
                )
            )
        ).check(
            matches(
                isDisplayed()
            )
        )

    }

    @Test
    fun showSnackBar() {

        // WHEN - ReminderListFragment launched to display Reminder with empty reminder
        val message = "Test Snack"
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment { it.testSnackBar(message) }

        // Going too fast to detect snackbar
        Thread.sleep(500)

        // THEN
        // Check Toast is correct
        onView(
            withId(
                com.google.android.material.R.id.snackbar_text
            )
        ).check(
            matches(
                withText("Test Snack")
            )
        )

    }
}