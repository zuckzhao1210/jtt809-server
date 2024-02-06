package org.wanji.protocol.codec;

import io.github.yezhihao.protostar.Schema;
import io.github.yezhihao.protostar.SchemaManager;
import io.github.yezhihao.protostar.schema.RuntimeSchema;
import io.github.yezhihao.protostar.util.ArrayMap;
import io.github.yezhihao.protostar.util.Explain;
import io.netty.buffer.*;
import io.netty.util.ByteProcessor;
import org.wanji.protocol.basic.JTMessage;
import org.wanji.protocol.commons.JTUtils;

import java.util.LinkedList;

/**
 * 具体的encoder实现类
 */
public class JTMessageEncoder {

    private static final ByteBufAllocator ALLOC = PooledByteBufAllocator.DEFAULT;

    private final SchemaManager schemaManager;

    private final ArrayMap<RuntimeSchema> headerSchemaMap;

    public JTMessageEncoder(String... basePackages) {
        this.schemaManager = new SchemaManager(basePackages);
        this.headerSchemaMap = schemaManager.getRuntimeSchema(JTMessage.class);
    }

    public JTMessageEncoder(SchemaManager schemaManager) {
        this.schemaManager = schemaManager;
        this.headerSchemaMap = schemaManager.getRuntimeSchema(JTMessage.class);
    }

    public ByteBuf encode(JTMessage message) {
        return encode(message, null);
    }

    public ByteBuf encode(JTMessage message, Explain explain) {
        int version = JTUtils.bytesToIntBigEndian(message.getVersionFlag());
        int headLength = JTUtils.headerLength(version);
        int bodyLength = 0;

        Schema headSchema = headerSchemaMap.get(version);
        Schema bodySchema = schemaManager.getRuntimeSchema(message.getMsgId(), version);

        ByteBuf output;
        if (bodySchema != null) {
            output = ALLOC.buffer(headLength + bodySchema.length());
            output.writerIndex(headLength);
            bodySchema.writeTo(output, message, explain);
            bodyLength = output.writerIndex() - headLength;
        } else {
            output = ALLOC.buffer(headLength, 21);
        }

        if (bodyLength <= 1023) {
            message.setBodyLength(bodyLength);

            int writerIndex = output.writerIndex();
            if (writerIndex > 0) {
                output.writerIndex(0);
                headSchema.writeTo(output, message, explain);
                output.writerIndex(writerIndex);
            } else {
                headSchema.writeTo(output, message, explain);
            }

            output = sign(output);
            output = escape(output);

        } else {
            ByteBuf[] slices = slices(output, headLength, 1023);
            int total = slices.length;

            CompositeByteBuf _allBuf = new CompositeByteBuf(ALLOC, false, total);
            output = _allBuf;

//            message.setSubpackage(true);
//            message.setPackageTotal(total);

            headLength = JTUtils.headerLength(version);
            for (int i = 0; i < total; i++) {
                ByteBuf slice = slices[i];

                message.setMsgSn(i + 1);
                message.setBodyLength(slice.readableBytes());

                ByteBuf headBuf = ALLOC.buffer(headLength, headLength);
                headSchema.writeTo(headBuf, message, explain);
                ByteBuf msgBuf = new CompositeByteBuf(ALLOC, false, 2)
                        .addComponent(true, 0, headBuf)
                        .addComponent(true, 1, slice);
                msgBuf = sign(msgBuf);
                msgBuf = escape(msgBuf);
                _allBuf.addComponent(true, i, msgBuf);
            }
        }
        return output;
    }

    public static ByteBuf[] slices(ByteBuf output, int start, int unitSize) {
        int totalSize = output.writerIndex() - start;
        int tailIndex = (totalSize - 1) / unitSize;

        ByteBuf[] slices = new ByteBuf[tailIndex + 1];
        output.skipBytes(start);
        for (int i = 0; i < tailIndex; i++) {
            slices[i] = output.readSlice(unitSize);
        }
        slices[tailIndex] = output.readSlice(output.readableBytes());
        output.retain(tailIndex);
        return slices;
    }

    /** 签名 */
    public static ByteBuf sign(ByteBuf buf) {
        byte checkCode = JTUtils.bcc(buf, 0);
        buf.writeByte(checkCode);
        return buf;
    }

    private static final ByteProcessor searcher = value -> !(value == 0x5b || value == 0x5d || value == 0x5a || value == 0x5e);

    /** 转义处理 */
    public static ByteBuf escape(ByteBuf source) {
        int low = source.readerIndex();
        int high = source.writerIndex();

        LinkedList<ByteBuf> bufList = new LinkedList();
        int mark, len;
        while ((mark = source.forEachByte(low, high - low, searcher)) > 0) {
            // 如果遇到四种规定的字节
            len = mark + 1 - low;
            // 裁切
            ByteBuf[] slice = slice(source, low, len);
            bufList.add(slice[0]);  // 转义前字符之前的数据，包括转义字符
            bufList.add(slice[1]);  // 转义字符后跟着的 01 or 02
            low += len;
        }

        // 加上最后一段字符
        if (bufList.size() > 0) {
            bufList.add(source.slice(low, high - low));
        } else {
            bufList.add(source);
        }

        // 头尾标识
        ByteBuf delimiter1 = Unpooled.buffer(1, 1).writeByte(0x5b).retain();
        ByteBuf delimiter2 = Unpooled.buffer(1, 1).writeByte(0x5d).retain();
        bufList.addFirst(delimiter1);
        bufList.addLast(delimiter2);

        CompositeByteBuf byteBufs = new CompositeByteBuf(ALLOC, false, bufList.size());
        byteBufs.addComponents(true, bufList);
        return byteBufs;
    }


    /** 截断转义前报文，并转义 */
    protected static ByteBuf[] slice(ByteBuf byteBuf, int index, int length) {
        byte first = byteBuf.getByte(index + length - 1);   // 待转义的字符

        ByteBuf[] bufs = new ByteBuf[2];
        bufs[0] = byteBuf.retainedSlice(index, length); // 裁切转义前报文，bufs[0] 的最后一个字节是待转义字符 5a 5b 5e 或 5d

        if (first == 0x5a) {
            bufs[1] = Unpooled.buffer(1, 1).writeByte(0x02);
        } else if (first == 0x5b) {
            byteBuf.setByte(index + length - 1, 0x5a);
            bufs[1] = Unpooled.buffer(1, 1).writeByte(0x02);
        } else if (first == 0x5e) {
            bufs[1] = Unpooled.buffer(1, 1).writeByte(0x02);
        } else {
            byteBuf.setByte(index + length - 1, 0x5e);
            bufs[1] = Unpooled.buffer(1, 1).writeByte(0x01);
        }
        return bufs;
    }

}
