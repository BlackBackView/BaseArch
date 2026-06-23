package com.bbv.base.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.UUID;

/**
 * 设备 ID 生成工具
 * <p>
 * 优先级：serialId > imei > androidId > genUuid（随机生成并持久化）
 * <p>
 * 注意：
 * - 从 Android 10 起，无法获取非随机化的 serial（需要 READ_PHONE_STATE 权限）
 * - getDeviceId() 已简化为优先使用 Settings.System.ANDROID_ID
 */
public class DevicesIdUtils {
    public static final String TAG = DevicesIdUtils.class.getName();
    private static final String SF_ROOT_NAME = "HbDeviceId";
    private static final String SF_NAME = "deviceId";
    private static String DEVICE_ID = "";

    public static boolean isHaveDevicesId(Context context) {
        return getPrivateSf(context).getString(SF_NAME, null) != null;
    }

    /**
     * 获取设备 ID（优先使用缓存的 ANDROID_ID，无则重新生成）
     */
    public static String getDeviceId(Context context) {
        if (!TextUtils.isEmpty(DEVICE_ID)) {
            return DEVICE_ID;
        }
        String finalID = getPrivateSf(context).getString(SF_NAME, null);
        if (TextUtils.isEmpty(finalID)) {
            // 优先使用 ANDROID_ID（无需额外权限）
            finalID = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
            if (TextUtils.isEmpty(finalID)) {
                finalID = genDeviceId(context);
            }
            getPrivateSf(context).edit().putString(SF_NAME, finalID).apply();
        }
        DEVICE_ID = finalID;
        return finalID;
    }

    public static String getDevicesIdReplaceLine(Context context) {
        return getDeviceId(context).replaceAll("-", "");
    }

    /**
     * 根据设备硬件信息生成唯一 ID
     * 优先级：serial > androidId > 随机 UUID
     */
    private static String genDeviceId(Context context) {
        String prefixId = null;
        String serialId = getSerialNum();
        String androidId = getAndroidId(context);

        if (!TextUtils.isEmpty(serialId) && serialId.length() >= 5 && !serialId.toLowerCase().contains("unknown")) {
            prefixId = serialId;
        } else if (!TextUtils.isEmpty(androidId) && !androidId.contains("9774d56d682e549c")) {
            prefixId = androidId;
        }

        if (!TextUtils.isEmpty(prefixId)) {
            try {
                return UUID.nameUUIDFromBytes(prefixId.getBytes("utf-8")).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return getGenUuid(context);
    }

    private static SharedPreferences getPrivateSf(Context context) {
        return context.getSharedPreferences(SF_ROOT_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 获取随机生成的 UUID（持久化保存）
     */
    public static String getGenUuid(Context context) {
        SharedPreferences sf = getPrivateSf(context);
        String uuid = sf.getString(SF_NAME, null);
        if (TextUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString().toUpperCase();
            sf.edit().putString(SF_NAME, uuid).apply();
        }
        return uuid;
    }

    private static String getAndroidId(Context context) {
        try {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取设备序列号
     * Android 10+ 需要 READ_PHONE_STATE 权限，可能返回 null
     */
    private static String getSerialNum() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Build.getSerial();
            } else {
                return Build.SERIAL;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
