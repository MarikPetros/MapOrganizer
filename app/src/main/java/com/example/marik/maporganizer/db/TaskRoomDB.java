package com.example.marik.maporganizer.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;


@Database(entities ={TaskItem.class}, version =1)

public abstract class TaskRoomDB extends RoomDatabase {

    public abstract TaskDao mDao();

    private static TaskRoomDB sInstance;

    public static TaskRoomDB getDatabase(final Context context) {
        if (sInstance == null) {
            synchronized (TaskRoomDB.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            TaskRoomDB.class, "word_database")
                            .build();                }
            }
        }
        return sInstance;
    }
}
