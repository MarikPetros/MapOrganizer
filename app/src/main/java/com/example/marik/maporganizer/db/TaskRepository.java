package com.example.marik.maporganizer.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Room;
import android.os.AsyncTask;
import android.widget.Toast;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class TaskRepository {
    private static TaskRepository REPO_INSTANCE;

    private TaskDao mDao;
    private LiveData<List<TaskItem>> mItemList;
    public static TaskItem mItem;

    private TaskRepository(Application application) {
        TaskDataBase db = TaskDataBase.getDataBase(application);
        /*TaskDataBase db = Room.databaseBuilder(application.getBaseContext(),
                TaskDataBase.class, "task_db").build();*/
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

    public List<TaskItem> getAllTaskItems() throws ExecutionException, InterruptedException {
        return (new GetAllTaskItemsAsyncTask(mDao).execute()).get();
    }

    public TaskItem getItemByLocation(double latitude, double longitude) {
        TaskItem taskItem = null;
        try {
            taskItem = (new GetByLocationAsyncTask(mDao).execute(latitude, longitude)).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return taskItem;
        /*new GetByLocationAsyncTask(mDao).execute(latitude, longitude);
        return mItem;*/
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
        protected Void doInBackground(TaskItem... taskItems) {
            mAsyncTaskDao.insert(taskItems[0]);
            return null;
        }
    }


    public static class UpdateAsyncTask extends AsyncTask<TaskItem, Void, Void> {

        private TaskDao mAsyncTaskDao;

        UpdateAsyncTask(TaskDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(TaskItem... taskItems) {
            mAsyncTaskDao.update(taskItems[0]);
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
        protected TaskItem doInBackground(Double... doubles) {
            return mAsyncTaskDao.getItemByLocation(doubles[0], doubles[1]);
        }

        @Override
        protected void onPostExecute(TaskItem item) {
            TaskRepository.mItem = item;
            super.onPostExecute(item);
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

    public static class GetAllTaskItemsAsyncTask extends AsyncTask<Void, Void, List<TaskItem>> {

        private TaskDao mAsyncTaskDao;

        GetAllTaskItemsAsyncTask(TaskDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<TaskItem> doInBackground(Void... params) {
            return mAsyncTaskDao.getAllTaskItems();
        }
    }
}

