package com.dmitry.weatherwidget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class WeatherWorker extends Worker {

    public WeatherWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        WeatherApiClient.fetchWeatherSync(getApplicationContext());
        return Result.success();
    }

    public static void setupPeriodicWork(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                WeatherWorker.class, 2, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);
    }
}
