package org.wanji.protocol.codec;

import io.github.yezhihao.protostar.SchemaManager;
import io.github.yezhihao.protostar.schema.RuntimeSchema;
import io.github.yezhihao.protostar.util.ArrayMap;
import io.github.yezhihao.protostar.util.Explain;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ByteProcessor;
import org.wanji.protocol.basic.JTMessage;
import org.wanji.protocol.commons.DataTypeIndex;
import org.wanji.protocol.commons.JTUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 具体的Decoder实现类
 */
public class JTMessageDecoder {
    private static final ByteBufAllocator ALLOC = PooledByteBufAllocator.DEFAULT;
    // 0x5B
    public static final byte[] START_DELIMITER = new byte[]{91};
    // 0x5D
    public static final byte[] END_DELIMITER = new byte[]{93};

    /**
     * schemaManager是一个管理和查找RuntimeSchema对象的注册表，允许通过类型ID，类型，类名和版本号检索和管理RuntimeSchema
     * 不同版本对应不同的RuntimeSchema，暂定的 2013 版本为 0， 2019 版本从 1 开始
     */
    private final SchemaManager schemaManager;

    private final ArrayMap<RuntimeSchema> headerSchemaMap;

    public JTMessageDecoder(String... basePackages) {
        this.schemaManager = new SchemaManager(basePackages);
        this.headerSchemaMap = schemaManager.getRuntimeSchema(JTMessage.class);
    }

    public JTMessageDecoder(SchemaManager schemaManager) {
        this.schemaManager = schemaManager;
        this.headerSchemaMap = schemaManager.getRuntimeSchema(JTMessage.class);
    }

    public JTMessage decode(ByteBuf input) {
        return decode(input, null);
    }

    /**
     * 解码器函数
     * @param input 输入字节流
     * @param explain explain
     * @return JTMessage
     */
    public JTMessage decode(ByteBuf input, Explain explain) {
        /* 第一步：反转义 */
        ByteBuf buf = unescape(input);

        /* 第二步：校验从数据头到校验码前的字节 */
        boolean verified = verify(buf);

        /* 第三步：解析数据头 */
        int msgId = buf.getUnsignedShort(8);
        int dataType = DataTypeIndex.get(msgId) > 0 ? buf.getUnsignedShort(DataTypeIndex.get(msgId)) : msgId;

        // 缺省值为 2011 版本, 0 表示注解的版本号空着
        int version = 0;
        // 根据版本获取数据头字节长度
        int headLen = JTUtils.headerLength(version);
        // 根据版本号获取数据头Scheme,根据消息ID和版本号获得消息体Schema
        RuntimeSchema<JTMessage> headSchema = headerSchemaMap.get(version);
        RuntimeSchema<JTMessage> bodySchema = schemaManager.getRuntimeSchema(dataType, version);

        JTMessage message;
        if (bodySchema == null)
            message = new JTMessage();
        else
            message = bodySchema.newInstance();
        // 保存是否验证正确
        message.setVerified(verified);
        // 保存反转义之前的原始数据包
        message.setPayload(input);

        int writerIndex = buf.writerIndex();
        buf.writerIndex(headLen);
        headSchema.mergeFrom(buf, message, explain);
        buf.writerIndex(writerIndex - 1);
        // 将协议号从 [0,0,1] 转成 1
        int realVersion = JTUtils.bytesToIntBigEndian(message.getVersionFlag());

        if (realVersion != version)
            bodySchema = schemaManager.getRuntimeSchema(msgId, realVersion);

        if (bodySchema != null) {
           // int bodyLen = message.getBodyLength();
           // if (isSubpackage) {
           //
           //     ByteBuf bytes = ALLOC.buffer(bodyLen);
           //     buf.getBytes(headLen, bytes);
           //
           //     ByteBuf[] packages = addAndGet(message, bytes);
           //     if (packages == null)
           //         return message;
           //
           //     ByteBuf bodyBuf = Unpooled.wrappedBuffer(packages);
           //     bodySchema.mergeFrom(bodyBuf, message, explain);
           //     if (message.noBuffer()) {
           //         bodyBuf.release();
           //     }
           // } else {
            buf.readerIndex(headLen);
            bodySchema.mergeFrom(buf, message, explain);
           // }
        }
        return message;
    }

    protected ByteBuf[] addAndGet(JTMessage message, ByteBuf bytes) {
        return null;
    }

    /** 校验 */
    public static boolean verify(ByteBuf buf) {
        // crc16ccitt校验， tailOffset是末尾的偏移位，-3 标识不包含 校验 2位
        int checkCode = JTUtils.crc16ccitt(buf, -2);
        int realCheckCode = buf.getUnsignedShort(buf.writerIndex() - 2);
        return checkCode == realCheckCode;    // 校验位 2 个字节
    }

    private static final ByteProcessor searcher = value -> !(value == 0x5a || value == 0x5e);

    /**
     * 反转义函数，a) 0x5b --> 0x5a01   b) 0x5a --> 0x5a02
     *           c) 0x5d --> 0x5e01   d) 0x5e --> 0x5e02
     * @param source 原字节码
     * @return 返回反转义之后的字节码
     */
    public static ByteBuf unescape(ByteBuf source) {
        int low = source.readerIndex();
        int high = source.writerIndex();

        if (source.getByte(low) == START_DELIMITER[0])                                       // 去除起始和终止标识符
            low++;

        if (source.getByte(high - 1) == END_DELIMITER[0])
            high--;

        List<ByteBuf> bufList = new ArrayList<>();

        int mark, len;
        while ((mark = source.forEachByte(low, high-low, searcher)) > 0) {  // 按照5a或者5e来将数据切分成段
            len = mark + 2 - low;
            bufList.add(slice(source, low, len));                                  // 转义并且添加到队列中
            low = mark + 2;
        }
        bufList.add(source.slice(low, high - low));

        return new CompositeByteBuf(ALLOC, false, bufList.size(), bufList);  // 将切分的字节流拼接
    }

    /**
     * 分段转义函数，根据转义字符将字节流分割成多段后，每一小段以转义字符结尾的字节码的处理函数
     * @param byteBuf 输入字节码
     * @param index 起始索引
     * @param length 字节码长度
     * @return 返回转义后的一小段字节码
     */
    protected static ByteBuf slice(ByteBuf byteBuf, int index, int length) {

        byte delimiter = byteBuf.getByte(index + length - 2);               // 获取标识符和后缀
        byte suffix = byteBuf.getByte(index + length - 1);
        if (delimiter == 0x5a) {
            if (suffix == 0x02) {                                                 // 将 01 转成 5b  02 转成 5a
                return byteBuf.slice(index, length - 1);
            } else if (suffix == 0x01) {
                byteBuf.setByte(index + length - 2, 0x5b);
                return byteBuf.slice(index, length - 1);
            } else {
                return byteBuf.slice(index, length);                               // 如果既不是01 也不是02，目前是保留原来的样子
            }
        } else if (delimiter == 0x5e) {
            if (suffix == 0x02) {                                                  // 将 01 转成 5d  02 转成 5e
                return byteBuf.slice(index, length - 1);
            } else if (suffix == 0x01) {
                byteBuf.setByte(index + length - 2, 0x5d);
                return byteBuf.slice(index, length - 1);
            } else {
                return byteBuf.slice(index, length);
            }
        } else {
            // 如果escapedCode 不是转义字符，保留原来的样子
            return byteBuf.slice(index, length);
        }
    }
}
