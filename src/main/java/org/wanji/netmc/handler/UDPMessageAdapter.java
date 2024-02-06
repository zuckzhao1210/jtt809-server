package org.wanji.netmc.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanji.netmc.codec.Delimiter;
import org.wanji.netmc.session.Packet;
import org.wanji.netmc.session.Session;
import org.wanji.netmc.session.SessionManager;
import org.wanji.netmc.util.ByteBufUtils;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * UDP消息适配器
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
@ChannelHandler.Sharable
public class UDPMessageAdapter extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(UDPMessageAdapter.class);

    private final SessionManager sessionManager;

    private final long readerIdleTime;

    /**
     * 初始化函数
     * @param sessionManager    会话管理器
     * @param readerIdleTime    readerIdleTime
     * @param delimiters        标识位
     * @return UDPMessageAdapter    返回初始化实例
     */
    public static UDPMessageAdapter newInstance(SessionManager sessionManager, int readerIdleTime, Delimiter[] delimiters) {
        if (delimiters == null)
            return new UDPMessageAdapter(sessionManager, readerIdleTime);
        return new DelimiterBasedFrameImpl(sessionManager, readerIdleTime, delimiters);
    }

    private UDPMessageAdapter(SessionManager sessionManager, int readerIdleTime) {
        this.sessionManager = sessionManager;
        this.readerIdleTime = TimeUnit.SECONDS.toMillis(readerIdleTime);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        DatagramPacket packet = (DatagramPacket) msg;
        ByteBuf buf = packet.content();
        Session session = getSession(ctx, packet.sender());
        session.access();
        ctx.fireChannelRead(Packet.of(session, buf));               // 传递给下一个channel处理器
    }

    private final Map<InetSocketAddress, Session> sessionMap = new ConcurrentHashMap<>();

    private final Function<Session, Boolean> sessionRemover = session -> sessionMap.remove(session.remoteAddress(), session);

    protected Session getSession(ChannelHandlerContext ctx, InetSocketAddress sender) {
        Session session = sessionMap.get(sender);
        if (session == null) {
            session = sessionMap.computeIfAbsent(sender, _sender -> sessionManager.newInstance(ctx.channel(), _sender, sessionRemover));
            log.info("<<<<< Connected{}", session);
        }
        return session;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        /* 起一个线程，进行通道监控，监控心跳超时等情况 */
        Thread thread = new Thread(() -> {
            for (; ; ) {
                long nextDelay = readerIdleTime;
                long now = System.currentTimeMillis();

                for (Session session : sessionMap.values()) {
                    long time = readerIdleTime - (now - session.getLastAccessedTime());

                    if (time <= 0) {
                        log.warn(">>>>>终端心跳超时 {}", session);
                        session.invalidate();
                    } else {
                        nextDelay = Math.min(time, nextDelay);
                    }
                }
                try {
                    Thread.sleep(nextDelay);
                } catch (Throwable e) {
                    log.warn("IdleStateScheduler", e);
                }
            }
        });
        thread.setName(Thread.currentThread().getName() + "-c");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    private static class DelimiterBasedFrameImpl extends UDPMessageAdapter {

        private final Delimiter[] delimiters;

        private DelimiterBasedFrameImpl(SessionManager sessionManager, int readerIdleTime, Delimiter[] delimiters) {
            super(sessionManager, readerIdleTime);
            this.delimiters = delimiters;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            DatagramPacket packet = (DatagramPacket) msg;
            ByteBuf buf = packet.content();
            Session session = getSession(ctx, packet.sender());

            try {
                List<ByteBuf> out = decode(buf);
                for (ByteBuf t : out) {
                    ctx.fireChannelRead(Packet.of(session, t));
                }
            } catch (DecoderException e) {
                throw e;
            } catch (Exception e) {
                throw new DecoderException(e);
            } finally {
                buf.release();
            }
        }

        protected List<ByteBuf> decode(ByteBuf in) {
            List<ByteBuf> out = new LinkedList<>();
            while (in.isReadable()) {

                for (Delimiter delim : delimiters) {
                    int minDelimLength = delim.value.length;

                    int frameLength = ByteBufUtils.indexOf(in, delim.value);
                    if (frameLength >= 0) {

                        if (delim.strip) {
                            if (frameLength != 0)
                                out.add(in.readRetainedSlice(frameLength));
                            in.skipBytes(minDelimLength);
                        } else {
                            if (frameLength != 0) {
                                out.add(in.readRetainedSlice(frameLength + minDelimLength));
                            } else {
                                in.skipBytes(minDelimLength);
                            }
                        }
                    } else {
                        int i = in.readableBytes();
                        if (i > 0)
                            out.add(in.readRetainedSlice(i));
                    }
                }
            }
            return out;
        }
    }
}