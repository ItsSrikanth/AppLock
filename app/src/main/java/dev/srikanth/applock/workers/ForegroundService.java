package dev.srikanth.applock.workers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import dev.srikanth.applock.R;
import dev.srikanth.applock.activities.BiometricActivity;
import dev.srikanth.applock.activities.MainActivity;
import dev.srikanth.applock.activities.SplashActivity;
import dev.srikanth.applock.preferences.SettingsPreferences;
import dev.srikanth.applock.utils.Constants;

public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final String TAG = "ForegroundService";
    private String packageName = "";
    private HashSet<Object> lockedAppsSet;
    private boolean usingIntent = false;
    private SettingsPreferences preferences;
    private String lockedApps,previousApp;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("App Lock")
                .setContentText("Service running")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }
    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        usingIntent = true;
        preferences = SettingsPreferences.getInstance();
        lockedApps = preferences.getLockedApps();
        previousApp = preferences.getPreviousApp();

        Log.e(TAG, " run: launch lockedapps ## " +lockedApps);
        Log.e(TAG, " run: launch previousapp ## " +previousApp);

        try {
            lockedAppsSet = new HashSet<>();
            String[] split = lockedApps.split("\\|");
            lockedAppsSet.addAll(Arrays.asList(split));
        } catch (Exception e) {
            e.printStackTrace();
            preferences.setLockedApps("");
        }

        final Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    while (runLockService()) {
                    }
                    Log.e(TAG, " run: launch" +packageName);
                    Intent launchIntent = new Intent(getApplicationContext(), BiometricActivity.class);
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS|
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    launchIntent.putExtra(Constants.PACKAGENAME, packageName);
                    PendingIntent pendingIntent =
                            PendingIntent.getActivity(getApplicationContext(), 0, launchIntent, 0);
                    try {
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        Log.e(TAG, "debugrun: "+e.getMessage());
                        onStartCommand(intent,flags,startId);
                        e.printStackTrace();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(TAG, "debugrun: "+e.getMessage());
                    onStartCommand(intent,flags,startId);
                }
                super.run();
            }
        };
        thread.start();
        return START_STICKY;
    }

    private boolean runLockService() {
        String topAppPackageName = getTopAppPackageName(getApplicationContext());

        if (topAppPackageName!=null&&!topAppPackageName.equals("dev.srikanth.applock")) {

            if (lockedAppsSet.contains(topAppPackageName)) {
                if (!previousApp.equals(topAppPackageName)) {
                    packageName = topAppPackageName;
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private String getTopAppPackageName(Context ctx) {

        String currentApp = "";
        long lastTimeUsed = 0;
        UsageStatsManager usm = (UsageStatsManager) ctx.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> applist = null;
        if (usm != null) {
            applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000, time);
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
}