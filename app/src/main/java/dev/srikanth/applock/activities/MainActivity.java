package dev.srikanth.applock.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.srikanth.applock.R;
import dev.srikanth.applock.adapters.RAdapter;
import dev.srikanth.applock.pojos.AppInfo;
import dev.srikanth.applock.preferences.SettingsPreferences;
import dev.srikanth.applock.utils.Constants;
import dev.srikanth.applock.workers.AppLockWorker;
import dev.srikanth.applock.workers.ForegroundService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final int USAGE_PERMISSION = 100, OVERLAY_PERMISSION = 101;
    private RecyclerView recyclerView;
    private RAdapter adapter;
    private List<AppInfo> allowedApps;
    private ProgressBar appsLoadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appsLoadingBar = findViewById(R.id.appsLoadingBar);

        if(usageStatsPermissionGranted()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
//                startActivityForResult(intent, OVERLAY_PERMISSION);
                showPermissionRequest("display over other apps",intent,OVERLAY_PERMISSION);
            }else {
//                startAppLockWorker();
                if (!isMyServiceRunning(ForegroundService.class)) {
                    startService();
                }
            }
        }else
            showPermissionRequest("usage access ", new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),USAGE_PERMISSION);

        setupRecyclerView();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerview);

        appsLoadingBar.setVisibility(View.VISIBLE);
        allowedApps = new ArrayList<>();
        new GetAppsTask().execute();

        adapter = new RAdapter(MainActivity.this, allowedApps);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    private List<AppInfo> getAppsLIst() {
        PackageManager pm = this.getPackageManager();
        List<AppInfo> appsList = new ArrayList<AppInfo>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        SettingsPreferences preferences = SettingsPreferences.getInstance();
        String lockedApps = preferences.getLockedApps();

        List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);
        for(ResolveInfo ri:allApps) {
            Log.e(TAG, "getAppsLIst: "+ri.activityInfo.packageName);
            if (!ri.activityInfo.packageName.equals("dev.srikanth.applock")) {
                AppInfo app = new AppInfo();
                app.setLabel(ri.loadLabel(pm));
                app.setPackageName(ri.activityInfo.packageName);
                app.setIcon(ri.activityInfo.loadIcon(pm));
                if (lockedApps.contains(ri.activityInfo.packageName))app.setLock(true);
                else app.setLock(false);
                appsList.add(app);
                Log.e(TAG, "getAppsLIst: " + app.getPackageName());
            }
        }
        return appsList;
    }

    @Override
    protected void onDestroy() {
//        startAppLockWorker();
        startService();
        super.onDestroy();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void showPermissionRequest(String message, final Intent intent, final int usagePermission) {
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(MainActivity.this)
                .setTitle("Permission Request")
                .setMessage("Allow "+message)
                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        MainActivity.this.finish();
                        startActivityForResult(intent,usagePermission);
                    }
                })
                .setCancelable(false)
                .create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        switch (requestCode){
            case USAGE_PERMISSION:
                if(usageStatsPermissionGranted()){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&!Settings.canDrawOverlays(this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        showPermissionRequest("display over other apps",intent,OVERLAY_PERMISSION);
                    }else {
//                        startAppLockWorker();
                        if (!isMyServiceRunning(ForegroundService.class)) {
                            startService();
                        }
//                    startActivity(new Intent(MainActivity.this, BiometricActivity.class));
                    }
                }else
                    showPermissionRequest("usage access ", new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),USAGE_PERMISSION);
                break;
            case OVERLAY_PERMISSION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&!Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    showPermissionRequest("display over other apps",intent,OVERLAY_PERMISSION);
                }else {
//                    startAppLockWorker();
                    if (!isMyServiceRunning(ForegroundService.class)) {
                        startService();
                    }
//                    startActivity(new Intent(MainActivity.this, BiometricActivity.class));
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean usageStatsPermissionGranted() {
        boolean granted = false;
        try {
            AppOpsManager appOps = (AppOpsManager) MainActivity.this
                    .getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), MainActivity.this.getPackageName());

            if (mode == AppOpsManager.MODE_DEFAULT) {
                granted = (MainActivity.this.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
            } else {
                granted = (mode == AppOpsManager.MODE_ALLOWED);
            }
        }
        catch (Exception e){
            return false;
        }
        return granted;
    }

    private void startAppLockWorker() {
        WorkManager.getInstance(getApplicationContext()).cancelUniqueWork(Constants.APPLOCKWORK);

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(AppLockWorker.class).build();
        WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(Constants.APPLOCKWORK, ExistingWorkPolicy.REPLACE,oneTimeWorkRequest);
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
//        serviceIntent.putExtra("inputExtra", "App Lock Service running");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    private class GetAppsTask extends AsyncTask<Void, ArrayList<AppInfo>, List<AppInfo>> {

        @Override
        protected List<AppInfo> doInBackground(Void... voids) {
            return getAppsLIst();
        }

        @Override
        protected void onPostExecute(List<AppInfo> appInfos) {
            appsLoadingBar.setVisibility(View.GONE);
            allowedApps.clear();
            allowedApps.addAll(appInfos);
            Collections.sort(allowedApps,Collections.<AppInfo>reverseOrder());
            Log.e(TAG, "onPostExecute: "+allowedApps );
            adapter = new RAdapter(MainActivity.this, allowedApps);
            recyclerView.setAdapter(adapter);
            super.onPostExecute(appInfos);
        }
    }
}
