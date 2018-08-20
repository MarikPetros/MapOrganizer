package com.example.marik.maporganizer.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

import com.example.marik.maporganizer.db.TaskRoomDB;

public class TaskAppWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        TaskRoomDB taskRoomDB = TaskRoomDB.getDatabase(context);
    }
}
