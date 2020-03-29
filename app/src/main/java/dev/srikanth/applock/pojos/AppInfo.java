package dev.srikanth.applock.pojos;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AppInfo implements Comparable<AppInfo>, Serializable {
    private CharSequence label;
    private CharSequence packageName;
    private Drawable icon;
    private boolean lock;

    public AppInfo() {
    }

    public CharSequence getLabel() {
        return label;
    }

    public void setLabel(CharSequence label) {
        this.label = label;
    }

    public CharSequence getPackageName() {
        return packageName;
    }

    public void setPackageName(CharSequence packageName) {
        this.packageName = packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    @Override
    public int compareTo(AppInfo appInfo) {
        return appInfo.getLabel().toString().compareTo(this.label.toString());
    }
}