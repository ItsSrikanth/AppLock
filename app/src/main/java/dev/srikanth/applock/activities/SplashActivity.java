package dev.srikanth.applock.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import dev.srikanth.applock.R;
import dev.srikanth.applock.pojos.AppInfo;
import dev.srikanth.applock.preferences.SettingsPreferences;
import dev.srikanth.applock.utils.Constants;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initPreferences();
        if (hasBiometric())askFingerprint();
        else {
            Intent launchIntent = new Intent(SplashActivity.this,MainActivity.class);
            launchIntent.setFlags(
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(launchIntent);
            SplashActivity.this.finish();
        }
    }

    private void initPreferences() {
        SharedPreferences bcoPreferences = getSharedPreferences(
                Constants.LOCKEDAPPS, 0);
        SettingsPreferences.getInstance().setPreferenceObject(bcoPreferences);
    }

    private void askFingerprint() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(SplashActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
                SplashActivity.this.finish();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
//                Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                Intent launchIntent = new Intent(SplashActivity.this,MainActivity.class);
                launchIntent.setFlags(
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(launchIntent);
                SplashActivity.this.finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
                SplashActivity.this.finish();
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
}
