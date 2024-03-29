package com.example.todolist.data

import androidx.room.*
import com.example.todolist.model.Task

@Dao
interface TaskDao {
    @Query("SELECT * from task_table")
    fun getAllTasks(): List<Task>

    @Query("SELECT * from task_table WHERE favorite = 1")
    fun getDoneTasks(): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: Task)

    @Query("UPDATE task_table SET title=:title, description=:description WHERE id = :id")
    fun updateTask(id: String, title: String?, description: String)

    @Query("UPDATE task_table SET favorite=:favorite WHERE id = :id")
    fun favoriteTask(id: String, favorite: Boolean)

    @Delete
    fun deleteTask(task: Task)
}
