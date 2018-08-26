
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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class TaskViewModel extends AndroidViewModel{

    private TaskRepository taskRepository ;
    private LiveData<List<TaskItem>> items;
    private MutableLiveData<TaskItem> item=new MutableLiveData<>();
    private TaskItem mItem;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        taskRepository = TaskRepository.getRepository(application);
    }


    public void setItem(TaskItem taskItem){
        item.setValue(taskItem);
    }

    public LiveData<List<TaskItem>> getItems() {
        if (items == null) {
            items = new MutableLiveData<>();
            loadItems();
        }
        return items;
    }

public TaskItem getItemByLocation(double latitude, double longitude){
        if(mItem==null){
            mItem=new TaskItem();
        }
        taskRepository.getItemByLocation(latitude, longitude);
        return null;

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

    public void deleteItem(UUID id){
        taskRepository.delete(id);
    }

    public void update(TaskItem taskItem){
        taskRepository.update(taskItem);
    }

    public void loadItems() {
        items =  taskRepository.getAllItems();
    }

    public void loadItem(UUID id){
        try {
            mItem = taskRepository.getById(id);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}






