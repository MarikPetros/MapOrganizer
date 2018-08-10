package com.example.marik.maporganizer.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;


import java.util.List;

public class TaskRepository {

    private TaskDao mDao;
    private LiveData<List<TaskItem>> mItemList;

          public   TaskRepository(Application application) {
        TaskRoomDB db = TaskRoomDB.getDatabase(application);
        mDao = db.mDao();
     //  mItemList = mDao.getAll();
    }
}

