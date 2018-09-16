package com.example.marik.maporganizer.db;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;


import java.util.List;
import java.util.UUID;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM task_item ORDER BY date")
    LiveData<List<TaskItem>> getAll();

    @Query("SELECT * FROM task_item ORDER BY date")
    List<TaskItem> getAllTaskItems();

    @Query("SELECT * FROM task_item WHERE _id = :id")
    TaskItem getById(UUID id);

    @Query("SELECT * FROM task_item  WHERE  latitude=:latitude AND longitude=:longitude")
    TaskItem getItemByLocation(double latitude, double longitude);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TaskItem item);

    @Update
    void update(TaskItem item);

    @Query("DELETE FROM task_item WHERE _id in (:id) ")
    void delete(UUID... id);

    @Query("DELETE FROM task_item")
    void deleteAll();
}
