package com.dmitry.weatherwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;
import java.util.concurrent.Executors;

public class WeatherWidget extends AppWidgetProvider {

    public static final String ACTION_REFRESH = "com.dmitry.weatherwidget.ACTION_REFRESH";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidgetData(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_REFRESH.equals(intent.getAction())) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, WeatherWidget.class));
            for (int id : ids) {
                updateWidgetData(context, manager, id);
            }
        }
    }

    private void updateWidgetData(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        Intent intentActivity = new Intent(context, MainActivity.class);
        intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingActivity = PendingIntent.getActivity(
                context, 0, intentActivity, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pendingActivity);

        Intent intentRefresh = new Intent(context, WeatherWidget.class);
        intentRefresh.setAction(ACTION_REFRESH);
        PendingIntent pendingRefresh = PendingIntent.getBroadcast(
                context, widgetId, intentRefresh, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.tv_update_time, pendingRefresh);

        manager.updateAppWidget(widgetId, views);

        Executors.newSingleThreadExecutor().execute(() -> {
            WeatherApiClient.WeatherData data = WeatherApiClient.fetchWeatherSync(context);
            if (data != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    views.setTextViewText(R.id.tv_city, data.cityName);
                    views.setTextViewText(R.id.tv_temp, String.format("%.0f°", data.temp));
                    views.setTextViewText(R.id.tv_condition, data.conditionText);
                    views.setTextViewText(R.id.tv_details, "Только что");
                    manager.updateAppWidget(widgetId, views);
                });
            }
        });
    }
}
