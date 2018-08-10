package com.example.marik.maporganizer.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;


import java.util.List;

public class TaskRepository {

    private TaskDao mDao;
    private LiveData<List<TaskItem>> mItemList;

    public TaskRepository(Application application) {
        TaskRoomDB db = TaskRoomDB.getDatabase(application);
        mDao = db.mDao();
        mItemList = mDao.getAll();
    }

    LiveData<List<TaskItem>> getAllItems(){
        return mItemList;
    }
    public void insert(TaskItem taskItem){
        new InsertAsyncTask(mDao).execute(taskItem);
    }



    public static class InsertAsyncTask extends AsyncTask<TaskItem, Void, Void>{

        private TaskDao mAsyncTaskDao;

        InsertAsyncTask(TaskDao dao){
            mAsyncTaskDao=dao;
        }



        @Override
        protected Void doInBackground(TaskItem... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}

