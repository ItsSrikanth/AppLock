package dev.srikanth.applock.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkManager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.util.concurrent.Executor;

import dev.srikanth.applock.R;
import dev.srikanth.applock.preferences.SettingsPreferences;
import dev.srikanth.applock.utils.Constants;
import dev.srikanth.applock.workers.AppLockWorker;
import dev.srikanth.applock.workers.ForegroundService;

public class BiometricActivity extends AppCompatActivity {

    private static final String TAG = "BiometricActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometric);
        Log.e(TAG, "onCreate: " );
        try {
            if (hasBiometric()) {
                askFingerprint();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
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
    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        getApplicationContext().stopService(serviceIntent);
    }

    private void askFingerprint() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(BiometricActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
//                startAppLockWorker();
//                BiometricActivity.this.finish();
                startService();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
//                Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                String packageName = getIntent().getStringExtra(Constants.PACKAGENAME);
                if (packageName!=null) {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                    try {
                        SettingsPreferences preferences = SettingsPreferences.getInstance();
                        preferences.setPreviousApp(packageName);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    Log.e(TAG, "onAuthenticationSucceeded: "+packageName );
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    BiometricActivity.this.finish();
//                    startAppLockWorker();
                    startActivity(launchIntent);
                    startService();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
//                startAppLockWorker();
//                BiometricActivity.this.finish();
                startService();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("App Lock")
                .setSubtitle("Touch the FingerPrint Sensor")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);

    }

    private boolean hasBiometric() {
        BiometricManager biometricManager = BiometricManager.from(this);
        return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startService();
//        startAppLockWorker();
    }

    private void startAppLockWorker() {
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(AppLockWorker.class).build();
        WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(Constants.APPLOCKWORK, ExistingWorkPolicy.REPLACE,oneTimeWorkRequest);
    }


/*    public void retryClicked(View view) {
//        stopService();
        if (hasBiometric()) {
            askFingerprint();
        }
    }*/

    public void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
//        serviceIntent.putExtra("inputExtra", "App Lock Service running");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
