
package com.example.marik.maporganizer.viewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Address;
import android.location.Location;
import android.support.annotation.NonNull;

import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.db.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class TaskViewModel extends AndroidViewModel {

    private TaskRepository taskRepository;
    private LiveData<List<TaskItem>> items;
    private MutableLiveData<TaskItem> item = new MutableLiveData<>();

    public TaskViewModel(@NonNull Application application) {
        super(application);
        taskRepository = TaskRepository.getRepository(application);
    }


    public void setItem(TaskItem taskItem) {
        item.setValue(taskItem);
    }

    public LiveData<List<TaskItem>> getItems() {
        if (items == null) {
            items = new MutableLiveData<>();
            loadItems();
        }
        return items;
    }

    public List<TaskItem> getAllTaskItems() {
        List<TaskItem> allItems = new ArrayList<>();
        try {
            allItems = taskRepository.getAllTaskItems();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return allItems;
    }

    public TaskItem getItemByLocation(double latitude, double longitude) {
        return taskRepository.getItemByLocation(latitude, longitude);
    }


    public void insertItem(TaskItem taskItem) {
        taskRepository.insert(taskItem);
    }

    public void deleteItem(UUID id) {
        taskRepository.delete(id);
    }

    public void update(TaskItem taskItem) {
        taskRepository.update(taskItem);
    }

    public void loadItems() {
        items = taskRepository.getAllItems();
    }

    public void loadItem(UUID id) {
        try {
            TaskItem mItem = taskRepository.getById(id);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}






