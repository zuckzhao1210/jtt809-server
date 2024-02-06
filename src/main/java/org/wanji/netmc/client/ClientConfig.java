package org.wanji.netmc.client;


import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.ObjectUtil;
import org.wanji.netmc.codec.Delimiter;
import org.wanji.netmc.codec.LengthField;
import org.wanji.netmc.codec.MessageDecoder;
import org.wanji.netmc.codec.MessageEncoder;
import org.wanji.netmc.session.SessionManager;

public class ClientConfig {

    protected final String ip;
    protected final int port;
    protected final int maxFrameLength;
    protected final Delimiter[] delimiters;
    protected final LengthField lengthField;
    protected final MessageDecoder decoder;
    protected final MessageEncoder encoder;
    protected final ChannelInboundHandlerAdapter adapter;
    protected final HandlerMapping handlerMapping;
    protected final String name;
    protected final SessionManager sessionManager;
    // protected final boolean enableUDP;
    protected final TCPClient client;

    private ClientConfig(String ip,
                         int port,
                         int maxFrameLength,
                         Delimiter[] delimiters,
                         LengthField lengthField,
                         MessageDecoder decoder,
                         MessageEncoder encoder,
                         HandlerMapping handlerMapping,
                         SessionManager sessionManager,
                         String name
    ) {
        ObjectUtil.checkNotNull(maxFrameLength, "maxFrameLength");
        ObjectUtil.checkPositive(maxFrameLength, "maxFrameLength");
        if (delimiters == null && lengthField == null) {
            throw new IllegalArgumentException("At least one of delimiters and lengthField is not empty");
        }
        this.ip = ip;
        this.port = port;
        this.maxFrameLength = maxFrameLength;
        this.delimiters = delimiters;
        this.lengthField = lengthField;
        this.decoder = decoder;
        this.encoder = encoder;
        this.handlerMapping = handlerMapping;
        this.adapter = new TCPClientHandler(this.handlerMapping);
        this.sessionManager = sessionManager != null ? sessionManager : new SessionManager();
        this.name = name != null ? name : "TCP";
        this.client = new TCPClient(this);
    }

    public static Builder custom() {
        return new Builder();
    }

    public static class Builder {

        private String ip;
        private int port;
        private int maxFrameLength;
        private Delimiter[] delimiters;
        private LengthField lengthField;
        private MessageDecoder decoder;
        private MessageEncoder encoder;
        private HandlerMapping handlerMapping;
        private SessionManager sessionManager;
        private String name;

        public Builder() {
        }

        public Builder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setMaxFrameLength(int maxFrameLength) {
            this.maxFrameLength = maxFrameLength;
            return this;
        }

        public Builder setDelimiters(Delimiter[] delimiters) {
            this.delimiters = delimiters;
            return this;
        }

        public Builder setLengthField(LengthField lengthField){
            this.lengthField = lengthField;
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

        public Builder setSessionManager(SessionManager sessionManager) {
            this.sessionManager = sessionManager;
            return this;
        }
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public ClientConfig build() {
            return new ClientConfig(
                    this.ip,
                    this.port,
                    this.maxFrameLength,
                    this.delimiters,
                    this.lengthField,
                    this.decoder,
                    this.encoder,
                    this.handlerMapping,
                    this.sessionManager,
                    this.name
            );
        }
    }
}