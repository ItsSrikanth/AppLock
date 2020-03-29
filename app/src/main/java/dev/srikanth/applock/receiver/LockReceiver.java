package dev.srikanth.applock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import dev.srikanth.applock.workers.ForegroundService;

public class LockReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(LockReceiver.class.getSimpleName(), "Service restart");
        context.startService(new Intent(context, ForegroundService.class));
    }
}