package com.example.marik.maporganizer.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.db.TaskDataBase;
import com.example.marik.maporganizer.db.TaskItem;

import java.util.ArrayList;
import java.util.List;

public class ListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

