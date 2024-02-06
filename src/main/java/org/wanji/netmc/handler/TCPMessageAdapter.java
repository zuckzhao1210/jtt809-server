package org.wanji.netmc.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanji.netmc.session.Packet;
import org.wanji.netmc.session.Session;
import org.wanji.netmc.session.SessionManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TCP消息适配器
 * @author yezhihao
 * <a href="https://gitee.com/yezhihao/jt808-server">...</a>
 */
@ChannelHandler.Sharable
public class TCPMessageAdapter extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(TCPMessageAdapter.class);

    private static final AttributeKey<Session> KEY = AttributeKey.newInstance(Session.class.getName());

    private final SessionManager sessionManager;

    public TCPMessageAdapter(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * 当缓冲区可读时，调用channelRead
     * @param ctx   上下文
     * @param msg   报文
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        Session session = getSession(ctx);                  // 从 ctx 中获取 session（channel）
        ctx.fireChannelRead(Packet.of(session, buf));       // 将处理后的消息传递给下一个处理程序链中的处理器
    }

    private Session getSession(ChannelHandlerContext ctx) {
        Session session = ctx.channel().attr(KEY).get();
        // 如果没有获取到session，就 new 一个 session
        if (session == null) {
            Channel channel = ctx.channel();
            session = sessionManager.newInstance(channel);
            channel.attr(KEY).set(session);
        }
        return session;
    }

    /**
     * 当通道激活完成后 调用fireChannelActive方法，激活事件，触发此方法
     * @param ctx 上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("<<<<< Connected{}", ctx.channel().remoteAddress());
        Session session = getSession(ctx);                  // 从 ctx 中获取 session（channel）
        Long accessTime = session.access();                 // System.currentTimeMillis() 系统当前时间？没有做任何处理
        Date date = new Date(accessTime);                   // 显示登录时间为标准时间
        log.info("<<<<< 登录服务器的时间为 {}", new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(date));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Session session = ctx.channel().attr(KEY).get();
        if (session != null)
            session.invalidate();
        log.info(">>>>> Disconnected{}", client(ctx));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        if (e instanceof IOException)
            log.warn("<<<<<终端断开连接{} {}", client(ctx), e.getMessage());
        else
            log.warn(">>>>>消息处理异常" + client(ctx), e);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            log.warn(">>>>>终端心跳超时{} {}", event.state(), client(ctx));
            ctx.close();
        }
    }

    private static Object client(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        Session session = channel.attr(KEY).get();
        if (session != null)
            return session;
        return channel.remoteAddress();
    }
}