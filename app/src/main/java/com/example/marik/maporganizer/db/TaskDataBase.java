package com.example.marik.maporganizer.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {TaskItem.class}, version=2)
@TypeConverters(Converters.class)
public abstract class TaskDataBase extends RoomDatabase {

    private static String DB_NAME="task_db";

    public abstract TaskDao mDao();

    private static TaskDataBase sInstance;

    public static TaskDataBase getDataBase(Context context){
        if (sInstance == null) {
            synchronized (TaskDataBase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            TaskDataBase.class, DB_NAME).fallbackToDestructiveMigration()
                            .build();                }
            }
        }
        return sInstance;
    }
}

