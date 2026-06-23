package com.bbv.base.util;

import android.util.Base64;

import androidx.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密工具类
 *
 * 提供方法：
 * - AES/CBC/PKCS5Padding 加解密（带 IV + HMAC 完整性校验）
 * - HMAC-SHA256 哈希
 * - Base64 编解码
 * - 16 进制字符串与 byte[] 互转
 * - MD5
 */
public class CodesUtils {
    public enum EncodeType {
        BASE_64, HEX
    }

    public static final String ALGORITHM_PCKS5PADDING = "AES/CBC/PKCS5Padding";
    public static final String KEY_ALGORITHM = "AES";
    private static final String STRING_FORMAT = "utf-8";
    public static final String HMAC_SHA_256 = "HmacSHA256";

    private static final int IV_LENGTH = 16;
    private static final int HMAC_HASH_LENGTH = 32;

    // ==================== 对称加解密（含 HMAC 校验） ====================

    /**
     * AES/CBC/PKCS5Padding 解密，自动识别 Base64 / Hex 编码
     *
     * @param encrpytTxt 密文
     * @param key        密钥（16/24/32 字节）
     * @param encodeType 编码方式
     * @return 明文，失败返回 null
     */
    public @Nullable
    static String decrypt(String encrpytTxt, String key, EncodeType encodeType) {
        try {
            byte[] source;
            if (encodeType == EncodeType.BASE_64) {
                source = base64Decode(encrpytTxt);
            } else {
                source = hexToByte(encrpytTxt);
            }

            byte[] iv_byte = Arrays.copyOfRange(source, 0, IV_LENGTH);
            byte[] ase_byte = Arrays.copyOfRange(source, IV_LENGTH + HMAC_HASH_LENGTH, source.length);
            byte[] decrypt = decrypt(key.getBytes(STRING_FORMAT), iv_byte, ase_byte);
            if (decrypt != null) {
                return new String(decrypt);
            }
        } catch (IndexOutOfBoundsException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES/CBC/PKCS5Padding 加密，自动附加 IV + HMAC 并编码输出
     *
     * @param source     明文
     * @param key        密钥
     * @param encodeType 输出编码方式
     * @return 密文，失败返回 null
     */
    public @Nullable
    static String encrypt(String source, String key, EncodeType encodeType) {
        String iv = makeRawIv();
        try {
            byte[] encryptAesText = encrypt(key, iv, source.getBytes(STRING_FORMAT));
            byte[] hmac = hashHmacSha256(key, source);
            byte[] content = append(append(iv.getBytes(), hmac), encryptAesText);
            if (encodeType == EncodeType.BASE_64) {
                return new String(base64Encode(content));
            } else {
                return bytes2Hex(content);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== 原始 AES 加解密 ====================

    public @Nullable
    static byte[] decrypt(String key, String iv, byte[] needDecrypt) {
        try {
            return decrypt(key.getBytes(STRING_FORMAT), iv.getBytes(STRING_FORMAT), needDecrypt);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public @Nullable
    static byte[] decrypt(byte[] key_bytes, byte[] iv_bytes, byte[] needDecrypt) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_PCKS5PADDING);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(key_bytes, KEY_ALGORITHM),
                    new IvParameterSpec(iv_bytes));
            return cipher.doFinal(needDecrypt);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                 | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public @Nullable
    static byte[] encrypt(String key, String iv, byte[] needEncrypt) {
        try {
            return encrypt(key.getBytes(STRING_FORMAT), iv.getBytes(STRING_FORMAT), needEncrypt);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public @Nullable
    static byte[] encrypt(byte[] key_bytes, byte[] iv_bytes, byte[] needEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_PCKS5PADDING);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(key_bytes, KEY_ALGORITHM),
                    new IvParameterSpec(iv_bytes));
            return cipher.doFinal(needEncrypt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== HMAC-SHA256 ====================

    public @Nullable
    static byte[] hashHmacSha256(String key, String text) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(STRING_FORMAT), HMAC_SHA_256);
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(secretKeySpec);
            return mac.doFinal(text.getBytes(STRING_FORMAT));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== Base64 ====================

    public static byte[] base64Decode(String codeTextBase64) {
        return Base64.decode(codeTextBase64, Base64.NO_WRAP);
    }

    public static byte[] base64Encode(byte[] content) {
        return Base64.encode(content, Base64.NO_WRAP);
    }

    // ==================== Hex 转换 ====================

    /**
     * byte 数组转 16 进制字符串
     */
    public static String bytes2Hex(byte[] bts) {
        StringBuilder des = new StringBuilder();
        String tmp;
        for (byte bt : bts) {
            tmp = (Integer.toHexString(bt & 0xFF));
            if (tmp.length() == 1) {
                des.append("0");
            }
            des.append(tmp);
        }
        return des.toString();
    }

    /**
     * 16 进制字符串转 byte 数组
     */
    public static byte[] hexToByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

    // ==================== MD5 ====================

    private static final char[] MD5_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String md5(String source) {
        try {
            byte[] btInput = source.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char[] encrypted = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                encrypted[(k++)] = MD5_CHARS[(byte0 >>> 4 & 0xF)];
                encrypted[(k++)] = MD5_CHARS[(byte0 & 0xF)];
            }
            return new String(encrypted);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== 内部工具 ====================

    private static String makeRawIv() {
        return UUID.randomUUID().toString().substring(0, IV_LENGTH);
    }

    public static byte[] append(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
