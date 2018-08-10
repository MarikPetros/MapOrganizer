package com.example.marik.maporganizer.db;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;


import java.util.List;
import java.util.UUID;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM task_item")
    List<TaskItem> getAll();

    @Query("SELECT * FROM task_item WHERE mId = :id")
    TaskItem getById(UUID id);

    @Insert
    void insert(TaskItem item);

    @Update
    void update(TaskItem item);

    @Delete
    void delete(TaskItem item);

}
