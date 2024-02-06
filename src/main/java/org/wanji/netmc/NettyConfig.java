package org.wanji.netmc;

import io.netty.util.NettyRuntime;
import io.netty.util.internal.ObjectUtil;
import org.wanji.netmc.codec.Delimiter;
import org.wanji.netmc.codec.LengthField;
import org.wanji.netmc.codec.MessageDecoder;
import org.wanji.netmc.codec.MessageEncoder;
import org.wanji.netmc.core.HandlerInterceptor;
import org.wanji.netmc.core.HandlerMapping;
import org.wanji.netmc.session.SessionManager;

/**
 * Server的配置类
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
public class NettyConfig {

    protected final int workerCore;                                 // worker 线程数
    protected final int businessCore;                               //
    protected final int readerIdleTime;
    protected final int writerIdleTime;
    protected final int allIdleTime;
    protected final Integer port;
    protected final Integer maxFrameLength;
    protected final LengthField lengthField;
    protected final Delimiter[] delimiters;                         // 头尾标识符号
    protected final MessageDecoder decoder;                         // decoder
    protected final MessageEncoder encoder;                         // encoder
    protected final HandlerMapping handlerMapping;                  // handlerMapping， getHandler方法，通过msgId获取 handler
    protected final HandlerInterceptor handlerInterceptor;          // 消息拦截器

    protected final SessionManager sessionManager;                  // 会话管理器
    protected final boolean enableUDP;                              // udp 标志

    protected final Server server;                                  // server实例

    protected final String name;                                    // name

    private NettyConfig(int workerGroup,
                        int businessGroup,
                        int readerIdleTime,
                        int writerIdleTime,
                        int allIdleTime,
                        Integer port,
                        Integer maxFrameLength,
                        LengthField lengthField,
                        Delimiter[] delimiters,
                        MessageDecoder decoder,
                        MessageEncoder encoder,
                        HandlerMapping handlerMapping,
                        HandlerInterceptor handlerInterceptor,
                        SessionManager sessionManager,
                        boolean enableUDP,
                        String name
    ) {
        ObjectUtil.checkNotNull(port, "port");
        ObjectUtil.checkPositive(port, "port");
        ObjectUtil.checkNotNull(decoder, "decoder");
        ObjectUtil.checkNotNull(encoder, "encoder");
        ObjectUtil.checkNotNull(handlerMapping, "handlerMapping");
        ObjectUtil.checkNotNull(handlerInterceptor, "handlerInterceptor");
        if (!enableUDP) {
            ObjectUtil.checkNotNull(maxFrameLength, "maxFrameLength");
            ObjectUtil.checkPositive(maxFrameLength, "maxFrameLength");
            if (delimiters == null && lengthField == null) {
                throw new IllegalArgumentException("At least one of delimiters and lengthField is not empty");
            }
        }

        int processors = NettyRuntime.availableProcessors();
        this.workerCore = workerGroup > 0 ? workerGroup : processors + 2;
        this.businessCore = businessGroup > 0 ? businessGroup : Math.max(1, processors >> 1);
        this.readerIdleTime = readerIdleTime;
        this.writerIdleTime = writerIdleTime;
        this.allIdleTime = allIdleTime;
        this.port = port;
        this.maxFrameLength = maxFrameLength;
        this.lengthField = lengthField;
        this.delimiters = delimiters;
        this.decoder = decoder;
        this.encoder = encoder;
        this.handlerMapping = handlerMapping;
        this.handlerInterceptor = handlerInterceptor;
        this.sessionManager = sessionManager != null ? sessionManager : new SessionManager();
        this.enableUDP = enableUDP;

        if (enableUDP) {
            this.name = name != null ? name : "UDP";
            this.server = new UDPServer(this);
        } else {
            this.name = name != null ? name : "TCP";
            this.server = new TCPServer(this);
        }
    }

    /**
     * 初始化过程中已经完成了TCP/UDPServer的初始化，build返回
     * @return  Server Tcp或UDP服务器实例
     */
    public Server build() {
        return server;
    }

    public static Builder custom() {
        return new Builder();
    }

    public static class Builder {

        private int workerCore;
        private int businessCore;
        private int readerIdleTime = 240;
        private int writerIdleTime = 0;
        private int allIdleTime = 0;
        private Integer port;
        private Integer maxFrameLength;
        private LengthField lengthField;
        private Delimiter[] delimiters;
        private MessageDecoder decoder;
        private MessageEncoder encoder;
        private HandlerMapping handlerMapping;
        private HandlerInterceptor handlerInterceptor;
        private SessionManager sessionManager;
        private boolean enableUDP;
        private String name;

        public Builder() {
        }

        public Builder setThreadGroup(int workerCore, int businessCore) {
            this.workerCore = workerCore;
            this.businessCore = businessCore;
            return this;
        }

        public Builder setIdleStateTime(int readerIdleTime, int writerIdleTime, int allIdleTime) {
            this.readerIdleTime = readerIdleTime;
            this.writerIdleTime = writerIdleTime;
            this.allIdleTime = allIdleTime;
            return this;
        }

        public Builder setPort(Integer port) {
            this.port = port;
            return this;
        }

        public Builder setMaxFrameLength(Integer maxFrameLength) {
            this.maxFrameLength = maxFrameLength;
            return this;
        }

        public Builder setLengthField(LengthField lengthField) {
            this.lengthField = lengthField;
            return this;
        }

        public Builder setDelimiters(byte[][] delimiters) {
            Delimiter[] t = new Delimiter[delimiters.length];
            for (int i = 0; i < delimiters.length; i++) {
                t[i] = new Delimiter(delimiters[i]);
            }
            this.delimiters = t;
            return this;
        }

        public Builder setDelimiters(Delimiter... delimiters) {
            this.delimiters = delimiters;
            return this;
        }

        public Builder setDecoder(MessageDecoder decoder) {
            this.decoder = decoder;
            return this;
        }

        public Builder setEncoder(MessageEncoder encoder) {
            this.encoder = encoder;
            return this;
        }

        public Builder setHandlerMapping(HandlerMapping handlerMapping) {
            this.handlerMapping = handlerMapping;
            return this;
        }

        public Builder setHandlerInterceptor(HandlerInterceptor handlerInterceptor) {
            this.handlerInterceptor = handlerInterceptor;
            return this;
        }

        public Builder setSessionManager(SessionManager sessionManager) {
            this.sessionManager = sessionManager;
            return this;
        }

        public Builder setEnableUDP(boolean enableUDP) {
            this.enableUDP = enableUDP;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Server build() {
            return new NettyConfig(
                    this.workerCore,
                    this.businessCore,
                    this.readerIdleTime,
                    this.writerIdleTime,
                    this.allIdleTime,
                    this.port,
                    this.maxFrameLength,
                    this.lengthField,
                    this.delimiters,
                    this.decoder,
                    this.encoder,
                    this.handlerMapping,
                    this.handlerInterceptor,
                    this.sessionManager,
                    this.enableUDP,
                    this.name
            ).build();
        }
    }
}