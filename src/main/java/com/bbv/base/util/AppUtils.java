package com.bbv.base.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.List;

/**
 * App 相关工具类
 * <p>
 * 提供：应用信息查询、APK 安装/解析、设备信息、文件路径、IP 地址等通用方法。
 */
public class AppUtils {
    public static final String CHANNEL_PREFIX = "META-INF/aw_";
    public static String[] mChannelInfo;
    public static String mAppKey;
    public static long mAppId = 0;
    public static String mSdkVersion;

    // ==================== App 基本信息 ====================

    public static String getAppLabel(Context context) {
        CharSequence label = context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
        return label != null ? label.toString() : null;
    }

    public static Drawable getAppIcon(Context context) {
        return context.getPackageManager().getApplicationIcon(context.getApplicationInfo());
    }

    public static boolean isInstall(Context context, String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) return false;
        try {
            context.getPackageManager().getPackageInfo(pkgName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // ==================== Meta-Data 读取 ====================

    public static String getMetaDataString(Context context, String name) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            return info.metaData.getString(name);
        } catch (Throwable e) {
            Lg.INSTANCE.d("getMetaDataString error: " + e.getMessage(), "AppUtils");
            return null;
        }
    }

    public static int getMetaDataInt(Context context, String name) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            return info.metaData.getInt(name);
        } catch (Throwable e) {
            Lg.INSTANCE.d("getMetaDataInt error: " + e.getMessage(), "AppUtils");
            return 0;
        }
    }

    public static long getMetaDataLong(Context context, String name) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            return info.metaData.getLong(name);
        } catch (Throwable e) {
            Lg.INSTANCE.d("getMetaDataLong error: " + e.getMessage(), "AppUtils");
            return 0;
        }
    }

    // ==================== 渠道号（从 APK 的 ZIP 条目读取） ====================

    public static String[] getChannelByZipFileName(Context context) {
        if (mChannelInfo != null) return mChannelInfo;
        ApplicationInfo appinfo = context.getApplicationInfo();
        String sourceDir = appinfo.sourceDir;
        String ret = null;
        java.util.zip.ZipFile zipfile = null;
        try {
            zipfile = new java.util.zip.ZipFile(sourceDir);
            java.util.Enumeration<?> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                java.util.zip.ZipEntry entry = ((java.util.zip.ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.startsWith(CHANNEL_PREFIX)) {
                    ret = entryName;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipfile != null) {
                try { zipfile.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
        if (ret == null) return null;
        String[] split = ret.split("_");
        mChannelInfo = split.length >= 2
                ? java.util.Arrays.copyOfRange(split, 1, split.length)
                : new String[0];
        return mChannelInfo;
    }

    public static String getChannel(Context context) {
        String[] channelInfo = getChannelByZipFileName(context);
        if (channelInfo != null && channelInfo.length > 0) return channelInfo[0];
        return "DefaultChannel";
    }

    public static String getSubPackageId(Context context) {
        String[] channelInfo = getChannelByZipFileName(context);
        return (channelInfo != null && channelInfo.length > 1) ? channelInfo[1] : "0";
    }

    public static String getPackageId(Context context) {
        String[] channelInfo = getChannelByZipFileName(context);
        return (channelInfo != null && channelInfo.length > 2) ? channelInfo[2] : "0";
    }

    // ==================== APK 安装 / 解析 ====================

    /**
     * 安装 APK（适配 Android N+ FileProvider）
     */
    public static void installApp(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File apkFile = new File(filePath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileProvider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 解析本地 APK 文件信息
     */
    public static LocalApk dumpApkInfoPath(Context context, String path) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, 0);
        if (info != null) {
            if (Build.VERSION.SDK_INT >= 8) {
                info.applicationInfo.sourceDir = path;
                info.applicationInfo.publicSourceDir = path;
            }
            long versionCode;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = info.getLongVersionCode();
            } else {
                versionCode = info.versionCode;
            }
            return new LocalApk("0", info.packageName, info.versionName, versionCode, null, 0);
        }
        return null;
    }

    // ==================== 设备信息 ====================

    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    public static String getAndroidVersion() {
        return Build.VERSION.SDK;
    }

    /**
     * 是否是小米 8 + Android 9（特殊兼容处理）
     */
    public static boolean isBadXiaomi8() {
        return "xiaomi".equalsIgnoreCase(getDeviceBrand())
                && Build.VERSION.SDK_INT == Build.VERSION_CODES.P;
    }

    // ==================== 方向锁定 ====================

    /**
     * 根据当前屏幕方向锁定 Activity 方向
     */
    public static void lockOrientation(Activity activity) {
        int orientation = getOrientation(activity);
        try {
            activity.setRequestedOrientation(orientation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getOrientation(Activity activity) {
        Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int currentOrientation = activity.getResources().getConfiguration().orientation;
        int orientation = 0;
        switch (currentOrientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                orientation = (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
                        ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        : ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                orientation = (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270)
                        ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        : ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
        }
        return orientation;
    }

    // ==================== 文件路径 ====================

    /**
     * 获取外部下载目录（优先外部存储私有目录）
     */
    public static File getDownloadRoot(Context context) {
        if (Environment.isExternalStorageEmulated()) {
            return context.getExternalFilesDir("download");
        } else {
            File file = new File(context.getFilesDir(), "download");
            if (!file.exists()) file.mkdir();
            return file;
        }
    }

    public static String buildPath(String root, String fileName) {
        if (root == null) return null;
        if (TextUtils.isEmpty(fileName)) return root;
        boolean rootEndWithSeparator = root.charAt(root.length() - 1) == File.separatorChar;
        boolean fileNameFirstWithSeparator = fileName.charAt(0) == File.separatorChar;
        if (rootEndWithSeparator && fileNameFirstWithSeparator) {
            return root.substring(0, root.length() - 2) + fileName;
        } else if (rootEndWithSeparator || fileNameFirstWithSeparator) {
            return root + fileName;
        } else {
            return root + File.separator + fileName;
        }
    }

    // ==================== 进程判断 ====================

    /**
     * 判断当前进程是否为主进程
     */
    public static boolean isMainProcess(Context context) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
            String mainProcessName = context.getPackageName();
            int myPid = android.os.Process.myPid();
            for (ActivityManager.RunningAppProcessInfo info : processInfos) {
                if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== IP 地址 ====================

    public static String getIpAddressString() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface.getNetworkInterfaces();
                 enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI.getInetAddresses();
                     enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    // ==================== App 签名哈希 ====================

    /**
     * 获取当前 App 签名的 SHA-1 十六进制字符串
     */
    public static String getAppHash(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                md.update(signature.toByteArray());
                return CodesUtils.bytes2Hex(md.digest());
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
