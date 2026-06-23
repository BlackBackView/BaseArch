package com.bbv.base.util;

import android.graphics.drawable.Drawable;

/**
 * Apk 信息数据类
 * <p>
 * 用于表示设备上已安装或本地 APK 文件的基本信息。
 */
public class LocalApk {
    private String name;
    private String packageName;
    private String versionName;
    private long versionCode;
    private Drawable icon;
    private int flags;

    public LocalApk(String name, String packageName, String versionName, long versionCode, Drawable icon, int flags) {
        this.name = name;
        this.packageName = packageName;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.icon = icon;
        this.flags = flags;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public long getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(long versionCode) {
        this.versionCode = versionCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }
}
