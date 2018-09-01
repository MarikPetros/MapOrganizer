package com.example.marik.maporganizer.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.activity.MainActivity;
import com.example.marik.maporganizer.db.TaskDataBase;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.db.TaskRepository;
import com.example.marik.maporganizer.fragments.FragmentTaskCreation;

import java.util.Objects;


public class TaskAppWidgetProvider extends AppWidgetProvider {
    public static final String TOAST_ACTION = "com.example.marik.maporganizer.appwidget.TOAST_ACTION";
    public static final String EXTRA_ITEM = "com.example.marik.maporganizer.appwidget.EXTRA_ITEM";


    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        if (Objects.requireNonNull(intent.getAction()).equals(TOAST_ACTION)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            int viewIndex = intent.getIntExtra(EXTRA_ITEM, 0);
            Toast.makeText(context, "Touched view " + viewIndex, Toast.LENGTH_SHORT).show();
        }
        super.onReceive(context, intent);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // update each of the app widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {

            // Set up the intent that starts the StackViewService, which will
            // provide the views for this collection.
            Intent intent = new Intent(context, ListWidgetService.class);
            // Add the app widget ID to the intent extras.
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            // Instantiate the RemoteViews object for the app widget layout.
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.task_appwidget);
            // Set up the RemoteViews object to use a RemoteViews adapter.
            // This adapter connects
            // to a RemoteViewsService  through the specified intent.
            // This is how you populate the data.
            rv.setRemoteAdapter(R.id.task_list, intent);

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            rv.setEmptyView(R.id.task_list, R.id.empty_view);

            // Here we setup the a pending intent template. Individuals items of a collection
            // cannot setup their own pending intents, instead, the collection as a whole can
            // setup a pending intent template, and the individual items can set a fillInIntent
            // to create unique before on an item to item basis.
            Intent toastIntent = new Intent(context, TaskAppWidgetProvider.class);
            toastIntent.setAction(TaskAppWidgetProvider.TOAST_ACTION);
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.task_list, toastPendingIntent);
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds[i],
                    R.id.task_list);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
