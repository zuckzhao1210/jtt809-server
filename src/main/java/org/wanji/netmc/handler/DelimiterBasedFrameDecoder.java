package org.wanji.netmc.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.internal.ObjectUtil;
import org.wanji.netmc.codec.Delimiter;
import org.wanji.netmc.util.ByteBufUtils;

import java.util.List;

import static io.netty.util.internal.ObjectUtil.checkPositive;

/**
 * 标识位及frameDecoder，继承了ByteToMessageDecoder，而ByteToMessageDecoder继承了ChannelInboundHandlerAdapter
 * @author yezhihao
 * <a href="https://gitee.com/yezhihao/jt808-server">...</a>
 */
public class DelimiterBasedFrameDecoder extends ByteToMessageDecoder {

    /**
     * 标识符数组，eg. [5b, 5d]， 默认第一个为头标识，第二个为尾标识。
     */
    private Delimiter[] delimiters;

    /**
     * 标识符长度，静态属性
     */
    public static final int DELIMITER_LENGTH = 1;

    /**
     * 最大报文长度，默认为1500
     */
    public int MAX_FRAME_LENGTH = 1500;

    /**
     * 初始化函数
     * @param maxFrameLength    最大报文长度
     * @param delimiters        标识符
     */
    public DelimiterBasedFrameDecoder(int maxFrameLength, Delimiter... delimiters) {
        validateMaxFrameLength(maxFrameLength);
        ObjectUtil.checkNonEmpty(delimiters, "delimiters");
        this.delimiters = delimiters;
        this.MAX_FRAME_LENGTH = maxFrameLength;
        // this.failFast = failFast;
    }

    /**
     * 分包解码器
     * @param ctx   通道上下文环境
     * @param in    报文
     * @param out   解码信息的列表
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        Object decoded = decode(in);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    /**
     * 帧解析器的decode具体实现逻辑
     * @param buffer    报文
     * @return Object
     */
    protected Object decode(ByteBuf buffer) {
        // 头尾标识
        Delimiter startDelimiter =  delimiters[0];
        Delimiter endDelimiter = delimiters[1];

        //  查找起始，结束字符索引
        int startDelimiterIndex =  ByteBufUtils.indexOf(buffer, startDelimiter.getValue());
        int endDelimiterIndex = ByteBufUtils.indexOf(buffer, endDelimiter.getValue());
        ByteBuf frame = null;

        // 1. 如果找到了起始字符
        if (startDelimiterIndex >= 0) {
            // 1.1 先抛弃头标识前面的无效字符，再找结束字符
            buffer.skipBytes(startDelimiterIndex);
            // 1.2 起始字符在结束字符的前面，分包，提取
            if (startDelimiterIndex < endDelimiterIndex) {
                int frameLength = endDelimiterIndex - startDelimiterIndex + DELIMITER_LENGTH;
                // 如果长度符合最大报文长度
                if (frameLength < MAX_FRAME_LENGTH) {
                    frame = buffer.readRetainedSlice(frameLength);
                // 如果是超长报文，就抛弃
                } else {
                    buffer.skipBytes(frameLength);
                    throw new TooLongFrameException("frame length exceeds " + MAX_FRAME_LENGTH + ": " + frameLength + " - discarded!");
                }
            // 1.3 起始字符跑到结束字符的后面了，已经被丢弃了
            } else {
                // 重新尝试获取结束字符
                endDelimiterIndex = ByteBufUtils.indexOf(buffer, endDelimiter.getValue());
                // 如果没有找到结束字符且可读超长，就抛弃
                if (endDelimiterIndex < 0 && buffer.readableBytes() > MAX_FRAME_LENGTH) {
                    buffer.skipBytes(buffer.readableBytes());
                    throw new TooLongFrameException("frame length exceeds " + MAX_FRAME_LENGTH + ": " + buffer.readableBytes() + " - discarded!");
                //如果能找到结束字符，且报文不超长
                }
                return null;
            }
        // 2. 如果没有找到头标识，直接抛弃所有可读字节
        } else {
            buffer.skipBytes(buffer.readableBytes());
        }
        return frame;
    }

    private static void validateMaxFrameLength(int maxFrameLength) {
        checkPositive(maxFrameLength, "maxFrameLength");    // 检查 maxFrameLength 是否大于 0
    }
}