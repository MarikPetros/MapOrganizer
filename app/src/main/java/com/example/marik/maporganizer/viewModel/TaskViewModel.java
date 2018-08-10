package com.example.marik.maporganizer.viewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.example.marik.maporganizer.db.TaskItem;

import java.util.List;

public class TaskViewModel extends ViewModel{

    private MutableLiveData<List<TaskItem>> items;
    private MutableLiveData<TaskItem> mItem;

    public LiveData<List<TaskItem>> getItems() {
        if (items == null) {
            items = new MutableLiveData<List<TaskItem>>();
            loadItems();
        }
        return items;
    }

    public LiveData<TaskItem> getItem() {
        if (mItem == null) {
            mItem = new MutableLiveData<TaskItem>();
            loadItems();
        }
        return mItem;
    }
    private void loadItems() {
        // Do an asynchronous operation to fetch users.
    }

    private void loadItem(){

    }
}





