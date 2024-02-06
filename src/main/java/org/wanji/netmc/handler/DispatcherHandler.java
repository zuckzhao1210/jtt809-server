package org.wanji.netmc.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanji.netmc.core.HandlerInterceptor;
import org.wanji.netmc.core.HandlerMapping;
import org.wanji.netmc.core.handler.Handler;
import org.wanji.netmc.core.model.Message;
import org.wanji.netmc.session.Packet;
import org.wanji.netmc.session.Session;
import org.wanji.netmc.util.Stopwatch;

import java.util.concurrent.ExecutorService;

/**
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */

/**
 * 业务处理器
 */
@ChannelHandler.Sharable
public class DispatcherHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(DispatcherHandler.class);

    private final HandlerMapping handlerMapping;

    private final HandlerInterceptor interceptor;

    private final ExecutorService executor;

    public static boolean STOPWATCH = false;

    private static Stopwatch s;

    /**
     * DispatcherHandler初始化
     * @param handlerMapping 业务处理映射
     * @param interceptor 业务处理拦截器
     * @param executor Java线程池
     */
    public DispatcherHandler(HandlerMapping handlerMapping, HandlerInterceptor interceptor, ExecutorService executor) {
        if (STOPWATCH && s == null)
            s = new Stopwatch().start();
        this.handlerMapping = handlerMapping;
        this.interceptor = interceptor;
        this.executor = executor;
    }

    /**
     * 可读，通过报文中的msgID（子业务类型编码或者主业务类型编码）可以获取Handler，
     * 并执行业务处理流程。
     * @param ctx 通道上下文环境
     * @param msg 消息
     */
    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (STOPWATCH)
            s.increment();

        Packet packet = (Packet) msg;                                                   // packet 包含三个成员属性 session msg buffer
        Message request = packet.message;
        Handler handler = handlerMapping.getHandler(request.getMsgId());                // 通过业务类型 ID 获取业务处理器

        if (handler == null) {                                                          // 如果业务处理器为 null
            // Message response = interceptor.notSupported(request, packet.session);
            // if (response != null) {
            //     ctx.writeAndFlush(packet.replace(response));
            // }
        } else {
            if (handler.async) {
                executor.execute(() -> channelRead0(ctx, packet, handler));
            } else {
                channelRead0(ctx, packet, handler);
            }
        }
    }

    private void channelRead0(ChannelHandlerContext ctx, Packet packet, Handler handler) {
        Session session = packet.session;
        Message request = packet.message;
        Message response;
        long time = System.currentTimeMillis();

        try {
            if (!interceptor.beforeHandle(request, session))
                return;

            response = handler.invoke(request, session);
            if (handler.returnVoid) {
                // response = interceptor.successful(request, session);
            } else {
                interceptor.afterHandle(request, response, session);
            }
        } catch (Exception e) {
            log.warn(String.valueOf(request), e);
            // response = interceptor.exceptional(request, session, e);
        }
        time = System.currentTimeMillis() - time;
        if (time > 100)
            log.info("====={},慢处理耗时{}ms", handler, time);
        // if (response != null)
        //     ctx.writeAndFlush(packet.replace(response));
    }
}