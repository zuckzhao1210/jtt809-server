package org.wanji.netmc.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanji.netmc.codec.MessageDecoder;
import org.wanji.netmc.core.model.Message;
import org.wanji.netmc.session.Packet;

/**
 * 基础消息解码
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
@ChannelHandler.Sharable
public class MessageDecoderWrapper extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(MessageDecoderWrapper.class);

    private final MessageDecoder decoder;

    public MessageDecoderWrapper(MessageDecoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Packet packet = (Packet) msg;
        ByteBuf input = packet.take();
        try {
            Message message = decoder.decode(input, packet.session);
            if (message != null)
                ctx.fireChannelRead(packet.replace(message));
            input.skipBytes(input.readableBytes());
        } catch (Exception e) {
            log.error("消息解码异常[" + ByteBufUtil.hexDump(input, 0, input.writerIndex()) + "]", e);
            throw new DecoderException(e);
        } finally {
            input.release();
        }
    }
}