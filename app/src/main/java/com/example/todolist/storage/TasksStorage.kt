package com.example.todolist.storage

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.example.todolist.data.AppDatabase
import com.example.todolist.model.Task
import com.example.todolist.screens.tasks.TasksContract
import com.example.todolist.screens.tasks.TasksContract.TasksStorage.Filter.ALL
import com.example.todolist.screens.tasks.TasksContract.TasksStorage.Filter.FINISHED
import com.example.todolist.util.Constants.Companion.MODE_VIEW
import javax.inject.Inject

/* TODO: Move to the tasks package. */
class TasksStorage @Inject constructor(
    private val db: AppDatabase,
    context: Context? /* TODO: Why it is optional? */
) : TasksContract.TasksStorage {

    var sharedPref: SharedPreferences /* TODO: Type can be ommited. */ =
        PreferenceManager.getDefaultSharedPreferences(context)

    override suspend fun getTasks(filter: TasksContract.TasksStorage.Filter): List<Task> {
        return when (filter) {
            ALL -> db.taskDao().getAllTasks()
            FINISHED -> db.taskDao().getDoneTasks()
        }
    }

    override suspend fun insertTask(task: Task) = db.taskDao().insertTask(task)

    override suspend fun updateTask(task: Task) =
        db.taskDao().updateTask(task.id, task.title, task.descriptions, task.favorite)

    override suspend fun deleteTask(task: Task) = db.taskDao().deleteTask(task)

    override fun setFilterMode(filter: TasksContract.TasksStorage.Filter) =
        sharedPref.edit().putBoolean(MODE_VIEW, filter == ALL).apply()

    override fun getFilterMode(): TasksContract.TasksStorage.Filter =
        if (sharedPref.getBoolean(MODE_VIEW, true)) {
            ALL
        } else {
            FINISHED
        }
}

