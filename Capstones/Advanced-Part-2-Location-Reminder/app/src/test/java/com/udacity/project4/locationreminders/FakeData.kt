package com.udacity.project4.locationreminders

import com.udacity.project4.locationreminders.data.dto.ReminderDTO

object FakeData {

    val reminders = arrayListOf(
        ReminderDTO(
            "Title One",
            "This is a description",
            "Store",
            1.0,
            5.0
        ),
        ReminderDTO(
            "Title",
            "This is a description",
            "Mall",
            2.0,
            -3.0
        ),
        ReminderDTO(
            "Title",
            "This is a description",
            "Bank",
            3.0,
            -9.0
        ),
        ReminderDTO(
            "Title",
            "This is a description",
            "Restaurant",
            4.0,
            10.0
        ),
    )
}
