package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
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
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {

    private val database: RemindersDatabase by inject()
    private val repository: ReminderDataSource by inject()

    @Rule @JvmField
    var activityRule: ActivityTestRule<RemindersActivity?> = ActivityTestRule(RemindersActivity::class.java)


    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        stopKoin()

        startKoin {
            androidContext(getApplicationContext())
        }

    }

    @After
    fun clearUp() {
        database.close()
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
                        activityRule.getActivity()?.getWindow()?.getDecorView()
                    )
                )
            )
        ).check(
            matches(
                isDisplayed()
            )
        )

    }

}