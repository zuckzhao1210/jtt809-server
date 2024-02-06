package org.wanji.web.util;

//import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author zhaozhe
 * @date 2023/10/12 10:28
 */
public class SHA256 {
    public static final String SHA256 = "SHA-256";

    // 加盐的SHA256加密
    // 可能抛出 NoSuchAlgorithmException 错误，表示加密格式有误。
    public static String encrypt_SHA256_Salt(String str, String salt) throws NoSuchAlgorithmException{
        // 进行加盐加密后给他放回
        return encrypt_SHA256(encrypt_SHA256(str + salt) + salt);
    }

    // 普通的SHA256加密
    private static String encrypt_SHA256(String str) throws NoSuchAlgorithmException{
        // 获取加密格式
        MessageDigest instance = MessageDigest.getInstance(SHA256);
//        // 进行加密
//        instance.update(var.getBytes(StandardCharsets.UTF_8));
//        // 通过Hex的encodeHex函数，将每个byte都拆成了两个十六进制的数，并组合
//        return String.valueOf(Hex.encodeHex(instance.digest()));
        return "";
    }
}
