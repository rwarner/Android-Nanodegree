package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.KoinContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @After
    fun autoCloseKoinTest() {
        stopKoin()
    }

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
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
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        //Get our real repository
        repository = get() as ReminderDataSource

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

//    @Test
//    fun clickAddReminderFAB_verifyNavigateToSaveReminderFragment() {
//        // GIVEN - On the ReminderList screen
//        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
//        dataBindingIdlingResource.monitorFragment(scenario)
//
//        // Mock NavController
//        val navController = mock(NavController::class.java)
//        scenario.onFragment {
//            Navigation.setViewNavController(it.view!!, navController)
//        }
//
//        // WHEN - Click on the "+" button
//        onView(withId(R.id.addReminderFAB)).perform(click())
//
//        // THEN - Verify that it call navigate to SaveReminder screen
//        verify(navController).navigate(
//            ReminderListFragmentDirections.toSaveReminder()
//        )
//    }
//
//    @Test
//    fun noReminders_verifyShowNoData() {
//        // GIVEN - Reminder Items is empty
//
//        // WHEN - On the ReminderList screen
//        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
//        dataBindingIdlingResource.monitorFragment(scenario)
//
//        // THEN - Verify that no data is shown
//        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun reminders_verifyShowListOfReminders() {
//        // GIVEN - Insert a reminder.
//        val reminder = ReminderDTO("TITLE1", "DESCRIPTION", "Location", 1.0, 1.0)
//        runBlocking {
//            repository.saveReminder(reminder)
//        }
//
//        // WHEN - On the ReminderList screen
//        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
//        dataBindingIdlingResource.monitorActivity(scenario)
//
//        // THEN - Verify that reminder is shown
//        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))
//        onView(withText(reminder.title)).check(matches(isDisplayed()))
//        onView(withText(reminder.description)).check(matches(isDisplayed()))
//        onView(withText(reminder.location)).check(matches(isDisplayed()))
//    }
}