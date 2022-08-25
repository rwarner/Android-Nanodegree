package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.FakeData
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setUp() {
        // Apparently this is needed with Koin based on the Knowledge area
        stopKoin()

        // Setup our data sources
        fakeDataSource = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun loadReminders_ThatWeAreLoading() = runBlocking {
        // Pause dispatcher so you can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // WHEN we load up our reminders from the given data source
        viewModel.loadReminders()

        // THEN progress is showing
        assertThat(viewModel.showLoading.value, `is`(true))

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // THEN the progress indicator is hidden
        assertThat(viewModel.showLoading.value, `is`(false))
    }

    @Test
    fun loadReminders_Success() = runBlocking {
        // GIVEN some fake reminders
        FakeData.reminders.forEach { reminder ->
            // Save each reminder in our fake data source
            fakeDataSource.saveReminder(reminder)
        }

        // WHEN we load up our reminders from the given data source
        viewModel.loadReminders()

        // THEN
        val currentItemsInList = viewModel.remindersList.value
        if (currentItemsInList != null) {
            // Ensure that the list is the same as the items in the fake data
            assertThat(currentItemsInList.size, `is`(FakeData.reminders.size))
        }
        if (currentItemsInList != null) {
            for (i in currentItemsInList.indices) {
                // Make sure that each title is the same
                assertThat(currentItemsInList[i].title, `is`(FakeData.reminders[i].title))
            }
        }

        // Make sure that the showing no data value is in fact false
        assertThat(viewModel.showNoData.value, `is`(false))
    }

    @Test
    fun loadReminders_noReminders() = runBlocking {
        // GIVEN no reminders
        fakeDataSource.deleteAllReminders()

        // WHEN load reminders
        viewModel.loadReminders()

        // THEN

        // Make sure the size is zero
        val loadedItems = viewModel.remindersList.value
        if (loadedItems != null) {
            assertThat(loadedItems.size, `is`(0))
        }

        // Make sure the show no data is true
        assertThat(viewModel.showNoData.value, `is`(true))
    }

    @Test
    fun loadReminders_Error() = runBlocking {
        // GIVEN: the dataSource return errors.
        fakeDataSource.setReturnError(true)

        // WHEN load reminders
        viewModel.loadReminders()

        // THEN

        // show error message in SnackBar view
        assertThat(viewModel.showSnackBar.value, `is`("Error exception for data"))

        // showNoData is true
        assertThat(viewModel.showNoData.value, `is`(true))
    }
}