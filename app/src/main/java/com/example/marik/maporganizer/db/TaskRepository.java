package com.example.marik.maporganizer.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;


import java.util.List;
import java.util.UUID;

public class TaskRepository {
    private static TaskRepository REPO_INSTANCE;

    private TaskDao mDao;
    private LiveData<List<TaskItem>> mItemList;

    private TaskRepository(Application application) {
        TaskRoomDB db = TaskRoomDB.getDatabase(application);
        mDao = db.mDao();
        mItemList = mDao.getAll();
    }

    public static TaskRepository getRepository(Application application) {
        if (REPO_INSTANCE == null) {
            REPO_INSTANCE = new TaskRepository(application);
        }
        return REPO_INSTANCE;
    }

    public LiveData<List<TaskItem>> getAllItems() {
        return mItemList;
    }

    public TaskItem getById(UUID id) {
        return mDao.getById(id);
    }

    public void insert(TaskItem taskItem) {
        new InsertAsyncTask(mDao).execute(taskItem);
    }

    public void update(TaskItem taskItem) {
        new UpdateAsyncTask(mDao).execute(taskItem);
    }

    public void delete(TaskItem taskItem) {
        new DeleteAsyncTask(mDao).execute(taskItem);
    }


    public static class InsertAsyncTask extends AsyncTask<TaskItem, Void, Void> {

        private TaskDao mAsyncTaskDao;

        InsertAsyncTask(TaskDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(TaskItem... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public static class UpdateAsyncTask extends AsyncTask<TaskItem, Void, Void> {

        private TaskDao mAsyncTaskDao;

        UpdateAsyncTask(TaskDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(TaskItem... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }


    public static class DeleteAsyncTask extends AsyncTask<TaskItem, Void, Void> {

        private TaskDao mAsyncTaskDao;

        DeleteAsyncTask(TaskDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(TaskItem... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

}

