package com.example.marik.maporganizer.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;


@Database(entities ={TaskItem.class}, version =1)
@TypeConverters(Converters.class)
public abstract class TaskRoomDB extends RoomDatabase {

    private static String DB_NAME="task_database";

    public abstract TaskDao mDao();

    private static TaskRoomDB sInstance;

    public static TaskRoomDB getDatabase(final Context context) {
        if (sInstance == null) {
            synchronized (TaskRoomDB.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            TaskRoomDB.class, DB_NAME)
                            .build();                }
            }
        }
        return sInstance;
    }
}
