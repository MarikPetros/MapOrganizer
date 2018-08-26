package com.example.marik.maporganizer.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

import com.example.marik.maporganizer.db.TaskDataBase;


public class TaskAppWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        TaskDataBase taskRoomDB = TaskDataBase.getDataBase(context);
    }
}
