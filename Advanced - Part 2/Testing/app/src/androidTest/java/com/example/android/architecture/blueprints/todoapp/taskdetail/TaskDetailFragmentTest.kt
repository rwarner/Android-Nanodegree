package com.example.android.architecture.blueprints.todoapp.taskdetail

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.data.Task
import org.junit.Test
import org.junit.runner.RunWith
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.data.source.FakeAndroidTestRepository
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before


@RunWith(AndroidJUnit4::class)
@MediumTest
internal class TaskDetailFragmentTest {

    private lateinit var repository: TasksRepository

    @Before
    fun initRepository() {
        repository = FakeAndroidTestRepository()
        ServiceLocator.tasksRepository = repository
    }

    // Reset repository after each test
    @After
    fun cleanupDb() = runBlockingTest {
        ServiceLocator.resetRepository()
    }

    @Test
    fun activeTaskDetails_DisplayedInUI() = runBlockingTest {
        // GIVEN - Add active (incomplete task to the DB)
        val activeTask = Task("Active Task", "AndroidX Rocks", false)

        repository.saveTask(activeTask)

        // WHEN - Details fragment launched to display task
        val bundle = TaskDetailFragmentArgs(activeTask.id).toBundle()
        launchFragmentInContainer<TaskDetailFragment>(bundle, R.style.AppTheme)
        Thread.sleep(2000)

    }
}