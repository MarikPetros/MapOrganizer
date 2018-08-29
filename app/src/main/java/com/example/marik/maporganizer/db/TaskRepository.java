package com.example.marik.maporganizer.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class TaskRepository {
    private static TaskRepository REPO_INSTANCE;

    private TaskDao mDao;
    private LiveData<List<TaskItem>> mItemList;

    private TaskRepository(Application application) {
        TaskDataBase db = TaskDataBase.getDataBase(application);
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

    public TaskItem getById(UUID id) throws ExecutionException, InterruptedException {
        return (new LoadAsyncTask(mDao).execute(id)).get();
    }

    public TaskItem getItemByLocation(double latitude, double longitude) {
        TaskItem taskItem = null;
        try {
            taskItem = (new GetByLocationAsyncTask(mDao).execute(latitude, longitude).get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return taskItem;
    }


    public void insert(TaskItem taskItem) {
        new InsertAsyncTask(mDao).execute(taskItem);
    }

    public void update(TaskItem taskItem) {
        new UpdateAsyncTask(mDao).execute(taskItem);
    }

    public void delete(UUID id) {
        new DeleteAsyncTask(mDao).execute(id);
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


    public static class DeleteAsyncTask extends AsyncTask<UUID, Void, Void> {

        private TaskDao mAsyncTaskDao;

        DeleteAsyncTask(TaskDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(UUID... ids) {
            mAsyncTaskDao.delete(ids[0]);
            return null;
        }
    }

    public static class GetByLocationAsyncTask extends AsyncTask<Double, Void, TaskItem> {

        private TaskDao mAsyncTaskDao;

        GetByLocationAsyncTask(TaskDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected TaskItem doInBackground(Double... params) {
            return mAsyncTaskDao.getItemByLocation(params[0], params[1]);
        }
    }

    public static class LoadAsyncTask extends AsyncTask<UUID, Void, TaskItem> {

        private TaskDao mAsyncTaskDao;

        LoadAsyncTask(TaskDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected TaskItem doInBackground(UUID... params) {
            return mAsyncTaskDao.getById(params[0]);
        }
    }


}

