package dev.srikanth.applock.workers;

import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import dev.srikanth.applock.R;
import dev.srikanth.applock.activities.BiometricActivity;
import dev.srikanth.applock.preferences.SettingsPreferences;
import dev.srikanth.applock.utils.Constants;

public class AppLockWorker extends Worker {
    private static final String TAG = "AppLockWorker";
    private HashSet<String> lockedAppsSet;

    public AppLockWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    @Override
    public Result doWork() {
        Log.e(TAG, "doWork: "+new Date().toString());
        String topAppPackageName = getTopAppPackageName(getApplicationContext());
        SettingsPreferences preferences = SettingsPreferences.getInstance();

        if (topAppPackageName!=null&&!topAppPackageName.equals("dev.srikanth.applock")) {

            String lockedApps = preferences.getLockedApps();
            String previousApp = preferences.getPreviousApp();

            try {
                lockedAppsSet = new HashSet<>();
                String[] split = lockedApps.split("\\|");
                lockedAppsSet.addAll(Arrays.asList(split));

            } catch (Exception e) {
                e.printStackTrace();
                preferences.setLockedApps("");
                startAppLockWorker();
            }

            if (lockedAppsSet.contains(topAppPackageName)) {
                if (!previousApp.equals(topAppPackageName)) {
                    Intent intent = new Intent(getApplicationContext(), BiometricActivity.class);
                    intent.putExtra(Constants.PACKAGENAME, topAppPackageName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK  |
                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS |
                            Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    PendingIntent pendingIntent =
                            PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                    try {
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        Log.e(TAG, "debugrun: "+e.getMessage());
                        startAppLockWorker();
                        e.printStackTrace();
                    }
                    return Result.success();
                }
            }else {
                preferences.setPreviousApp("");
            }
        }
        startAppLockWorker();
        return Result.success();
    }

    private String getTopAppPackageName(Context ctx) {

        String currentApp = "";
        long lastTimeUsed = 0;
        UsageStatsManager usm = (UsageStatsManager) ctx.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> applist = null;
        if (usm != null) {
            applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000*1000, time);
        }
        if (applist != null && applist.size() > 0) {
            for (UsageStats usageStats : applist) {
                if(lastTimeUsed<usageStats.getLastTimeUsed()){
                    currentApp = usageStats.getPackageName();
                    lastTimeUsed = usageStats.getLastTimeUsed();
                }
            }
        }

        return currentApp;
    }

    private void startAppLockWorker() {
        /*Data workerData = new Data.Builder()
                .putString(Constants.LOCKEDAPPS, inputDataString)
                .build();*/

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(AppLockWorker.class).build();
        WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(Constants.APPLOCKWORK, ExistingWorkPolicy.REPLACE,oneTimeWorkRequest);
    }

}
