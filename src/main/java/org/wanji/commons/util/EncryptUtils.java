package org.wanji.commons.util;

/**
 * 加密工具类
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
public class EncryptUtils {
    public static int M1;
    public static int IA1;
    public static int IC1;

    /**
     * 加解密算法
     * @param key
     * @param bytes 数据部分字节流
     */
    public static void encrypt(int key, byte[] bytes) {
        int idx = 0;
        if ( 0 == key) {
            key = 1;
        }
        while (idx < bytes.length) {
            key = IA1 * (key % M1) + IC1;
            bytes[idx++] = (byte) (bytes[idx] ^ (key >> 20)&0xff);
        }
    }
}