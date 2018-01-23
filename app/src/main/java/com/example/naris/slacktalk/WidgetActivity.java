package com.example.naris.slacktalk;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Random;

public class WidgetActivity extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {


        final int count = appWidgetIds.length;


        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widgetactivity);
                Intent intent = new Intent(context, VoiceControlInput.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context,
                        0, intent, 0);
                remoteViews.setOnClickPendingIntent(R.id.voiceButton, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

    }

}

