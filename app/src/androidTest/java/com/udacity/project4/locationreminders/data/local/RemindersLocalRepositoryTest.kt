package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.CoroutineMain
import com.udacity.project4.RemDAO
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = CoroutineMain()

    val list = listOf(
        ReminderDTO(
            "title",
            "description",
            "location",
            (-360..360).random().toDouble(),
            (-360..360).random().toDouble()
        ),
        ReminderDTO(
            "title",
            "description",
            "location",
            (-360..360).random().toDouble(),
            (-360..360).random().toDouble()
        ),
        ReminderDTO(
            "title",
            "description",
            "location",
            (-360..360).random().toDouble(),
            (-360..360).random().toDouble()
        ),
        ReminderDTO(
            "title",
            "description",
            "location",
            (-360..360).random().toDouble(),
            (-360..360).random().toDouble()
        )
    )

    private lateinit var fakeRemindersDao: RemDAO
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun setup() {
        fakeRemindersDao = RemDAO()
        remindersLocalRepository = RemindersLocalRepository(
            fakeRemindersDao, Dispatchers.Unconfined
        )
    }

    @Test
    fun savesToLocalCache() = runBlockingTest {
        var list = mutableListOf<ReminderDTO>()
        list.addAll(fakeRemindersDao.remindersServiceData.values)
        assertThat(list).doesNotContain(list[3])
        assertThat((remindersLocalRepository.getReminders() as? Result.Success)?.data)
            .doesNotContain(
                list[3]
            )

        remindersLocalRepository.saveReminder(list[3])

        list = mutableListOf()
        list.addAll(fakeRemindersDao.remindersServiceData.values)
        assertThat(list).contains(list[3])

        val result = remindersLocalRepository.getReminders() as? Result.Success
        assertThat(result?.data).contains(list[3])
    }

    @Test
    fun getReminderByIdThatExistsInLocalCache() = runBlockingTest {
        assertThat((remindersLocalRepository.getReminder(list[0].id) as? Result.Error)?.message)
            .isEqualTo(
                "Reminder not found!"
            )

        fakeRemindersDao.remindersServiceData[list[0].id] = list[0]

        val loadedReminder =
            (remindersLocalRepository.getReminder(list[0].id) as? Result.Success)?.data

        Assert.assertThat<ReminderDTO>(loadedReminder as ReminderDTO, CoreMatchers.notNullValue())
        Assert.assertThat(loadedReminder.id, `is`(list[0].id))
        Assert.assertThat(loadedReminder.title, `is`(list[0].title))
        Assert.assertThat(loadedReminder.description, `is`(list[0].description))
        Assert.assertThat(loadedReminder.location, `is`(list[0].location))
        Assert.assertThat(loadedReminder.latitude, `is`(list[0].latitude))
        Assert.assertThat(loadedReminder.longitude, `is`(list[0].longitude))
    }

    @Test
    fun getReminderByIdThatDoesNotExistInLocalCache() = runBlockingTest {
        val message = (remindersLocalRepository.getReminder(list[0].id) as? Result.Error)?.message
        Assert.assertThat<String>(message, CoreMatchers.notNullValue())
        assertThat(message).isEqualTo("Reminder not found!")
    }

    @Test
    fun deleteAllReminders_EmptyListFetchedFromLocalCache() = runBlockingTest {
        assertThat((remindersLocalRepository.getReminders() as? Result.Success)?.data)
            .isEmpty()

        fakeRemindersDao.remindersServiceData[list[0].id] = list[0]
        fakeRemindersDao.remindersServiceData[list[1].id] = list[1]
        fakeRemindersDao.remindersServiceData[list[2].id] = list[2]

        assertThat((remindersLocalRepository.getReminders() as? Result.Success)?.data)
            .isNotEmpty()

        remindersLocalRepository.deleteAllReminders()

        assertThat((remindersLocalRepository.getReminders() as? Result.Success)?.data)
            .isEmpty()
    }
}