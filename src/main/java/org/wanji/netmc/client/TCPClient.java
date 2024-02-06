package org.wanji.netmc.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanji.netmc.handler.MessageDecoderWrapper;
import org.wanji.netmc.handler.MessageEncoderWrapper;
import org.wanji.netmc.handler.TCPMessageAdapter;

import static org.wanji.netmc.TCPServer.getByteToMessageDecoder;

/**
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
public class TCPClient {

    private static final Logger log = LoggerFactory.getLogger(TCPClient.class);
    private boolean isRunning;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private ClientConfig config;
    private String id;

    public TCPClient(ClientConfig config) {
        this.config = config;
    }

    private Bootstrap startInternal() {
        Bootstrap bootstrap = null;
        try {
            this.workerGroup = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(workerGroup);
            bootstrap.option(NioChannelOption.SO_REUSEADDR, true)
                    .option(NioChannelOption.TCP_NODELAY, true)
                    .option(NioChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<NioSocketChannel>() {

                        private final TCPMessageAdapter adapter = new TCPMessageAdapter(config.sessionManager);
                        private final MessageDecoderWrapper decoder = new MessageDecoderWrapper(config.decoder);
                        private final MessageEncoderWrapper encoder = new MessageEncoderWrapper(config.encoder);

                        @Override
                        public void initChannel(NioSocketChannel ch) {
                            ch.pipeline()
                                    .addLast("frameDecoder", frameDecoder())
                                    .addLast("adapter", adapter)
                                    .addLast(decoder)
                                    .addLast(encoder)
                                    .addLast("adapter", config.adapter);
                        }
                    });


        } catch (Exception e) {
            log.error("===TCP Client异常关闭", e);
        }
        return bootstrap;       // 有可能是null
    }

    public void writeObject(Object message) {
        channel.writeAndFlush(message);
    }

    public synchronized boolean start() throws InterruptedException {
        // 检查运行标识，如果已经启动，则返回
        if (isRunning) {
            log.warn("==={}已经启动,port:{}===", config.name, config.port);
            return isRunning;
        }
        // 如果没有在运行，则启动运行
        Bootstrap bootstrap = startInternal();

        ChannelFuture channelFuture = bootstrap.connect(config.ip, config.port).sync();
        this.channel = channelFuture.channel();
        this.channel.closeFuture();

        isRunning = channelFuture.isSuccess();
        if(isRunning)
            log.warn("===上级平台从链路 Client启动成功, id={}===", id);

        return isRunning;
    }

    public synchronized void stop() {
        workerGroup.shutdownGracefully();
        log.warn("===TCP Client已经停止, id={}===", id);
    }


    private ByteToMessageDecoder frameDecoder() {
        return getByteToMessageDecoder(config.lengthField, config.delimiters, config.maxFrameLength);
    }
}