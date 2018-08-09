package com.example.marik.maporganizer.viewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.example.marik.maporganizer.item.TaskItem;

import java.util.List;

public class TaskViewModel extends ViewModel{

//    private MutableLiveData<List<TaskItem>> items;
//    public LiveData<List<TaskItem>> getUsers() {
//        if (items == null) {
//            items = new MutableLiveData<List<TaskItem>>();
//            loadUsers();
//        }
//        return items;
//    }
//
//    private void loadUsers() {
//        // Do an asynchronous operation to fetch users.
//    }
private final MutableLiveData<TaskItem> mItem = new MutableLiveData<>();

    public LiveData<TaskItem> getItem() {
        return mItem;
    }

    public void setItem(TaskItem item) {
        mItem.setValue(item);
    }
}





