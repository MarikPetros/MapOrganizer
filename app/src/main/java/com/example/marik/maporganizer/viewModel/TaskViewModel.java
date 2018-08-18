/*
package com.example.marik.maporganizer.viewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.db.TaskRepository;

import java.util.List;
import java.util.UUID;

public class TaskViewModel extends AndroidViewModel{

    private TaskRepository taskRepository ;
    private LiveData<List<TaskItem>> items;
    private TaskItem mItem;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        taskRepository = TaskRepository.getRepository(application);
    }

    public LiveData<List<TaskItem>> getItems() {
        if (items == null) {
            items = new MutableLiveData<>();
            loadItems();
        }
        return items;
    }

    public TaskItem getItem(UUID  id) {
        if (mItem == null) {
            mItem = new TaskItem();
            loadItem(id);
        }
        return mItem;
    }

    public void insertItem(TaskItem taskItem){
        taskRepository.insert(taskItem);
    }

    public void deleteItem(TaskItem taskItem){
        taskRepository.delete(taskItem);
    }

    private void loadItems() {
        items =  taskRepository.getAllItems();
    }

    private void loadItem(UUID id){
        mItem = taskRepository.getById(id);
    }


}





*/
