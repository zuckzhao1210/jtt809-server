package org.wanji.netmc.codec;

/**
 * 头尾标识符类，5b，5d
 * @author yezhihao
 * <a href="https://gitee.com/yezhihao/jt808-server">...</a>
 */
public class Delimiter {
    /**
     * value字节数组
     */
    public final byte[] value;

    /**
     * 是否分片
     */
    public final boolean strip;

    public Delimiter(byte[] value) {
        this(value, true);
    }

    public Delimiter(byte[] value, boolean strip) {
        this.value = value;
        this.strip = strip;
    }

    // 返回字节数组
    public byte[] getValue() {
        return value;
    }

    public boolean isStrip() {
        return strip;
    }
}