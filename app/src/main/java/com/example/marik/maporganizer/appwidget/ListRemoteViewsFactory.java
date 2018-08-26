package com.example.marik.maporganizer.appwidget;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.activity.MainActivity;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.db.TaskRepository;
import com.example.marik.maporganizer.db.TaskRoomDB;
import com.example.marik.maporganizer.viewModel.TaskViewModel;

import java.util.ArrayList;
import java.util.List;

public class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context mContext;
    private List<TaskItem> mTaskWidgetItems = new ArrayList<>(); // Widgeti item piti lini
    private final int mAppWidgetId;
    private TaskRoomDB taskRoomDB;

    public ListRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        taskRoomDB = TaskRoomDB.getDatabase(mContext);
        mTaskWidgetItems.addAll(taskRoomDB.mDao().getAllTaskItems());
    }

    @Override
    public void onDataSetChanged() {
        //TODO
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
    }

    @Override
    public void onDestroy() {
        mTaskWidgetItems.clear();
    }

    @Override
    public int getCount() {
        return mTaskWidgetItems.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        // setting item's text
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
        rv.setTextViewText(R.id.widget_item_text, mTaskWidgetItems.get(position).getChoosedAddress());

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in TaskAppWidgetProvider.
        Bundle extras = new Bundle();
        extras.putInt(TaskAppWidgetProvider.EXTRA_ITEM, position);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_item_text, fillInIntent);

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;  /// indz tvum a karanq false dnenq
    }
}
