package org.wanji.protocol.commons;

import io.netty.buffer.ByteBuf;

public class JTUtils {

    /**
     * BCC校验(异或校验)
     */
    public static byte bcc(ByteBuf byteBuf, int tailOffset) {
        byte cs = 0;
        int readerIndex = byteBuf.readerIndex();
        int writerIndex = byteBuf.writerIndex() + tailOffset;
        while (readerIndex < writerIndex)
            cs ^= byteBuf.getByte(readerIndex++);
        return cs;
    }

    /**
     * CRC16 CCITT校验，从数据头到校验码前的 CRC 16-CCITT 校验值
     */
    public static int crc16ccitt(ByteBuf byteBuf, int tailOffset) {

        int crc = 0xffff;                                       // initial value
        int polynomial = 0x1021;                                // poly value

        int readerIndex = byteBuf.readerIndex();
        // readerIndex ++;                                      // 前面 FrameDecoder 已经去掉了头标识
        int writerIndex = byteBuf.writerIndex() + tailOffset;   // 不包含校验位2位

        while (readerIndex < writerIndex) {
            byte b = byteBuf.getByte(readerIndex++);
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ polynomial;
                } else {
                    crc <<= 1;
                }
            }
         }
        crc &= 0xffff;
        return crc;
    }

    /**
     *  @param version 版本号
     *  @return 如果版本是那么数据头一共有30个字节，如果版本是2023，数据头有22个字节
      */
    public static int headerLength(int version) {
        if (version > 0)
            return  30;
        else return 22;
    }


    public static int bytesToIntBigEndian(byte[] bytes) {
        // byte数组中序号大的在右边
        return (bytes[2] & 0xFF) |
                (bytes[1] & 0xFF) << 8 |
                (bytes[0] & 0xFF) << 16;
    }
}