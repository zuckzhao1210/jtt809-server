package org.wanji.netmc;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.wanji.netmc.handler.DispatcherHandler;
import org.wanji.netmc.handler.MessageDecoderWrapper;
import org.wanji.netmc.handler.MessageEncoderWrapper;
import org.wanji.netmc.handler.UDPMessageAdapter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * UDP服务端配置
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
public class UDPServer extends Server {

    protected UDPServer(NettyConfig config) {
        super(config);
    }

    protected AbstractBootstrap initialize() {
        // netty服务端初始化
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory(config.name, Thread.MAX_PRIORITY));
        if (config.businessCore > 0)
            businessGroup = new ThreadPoolExecutor(config.businessCore, config.businessCore, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new DefaultThreadFactory(config.name + "-B", true, Thread.NORM_PRIORITY));
        return new Bootstrap()
                .group(bossGroup)
                .channel(NioDatagramChannel.class)
                .option(NioChannelOption.SO_REUSEADDR, true)
                .option(NioChannelOption.SO_RCVBUF, 1024 * 1024 * 50)
                .handler(new ChannelInitializer<NioDatagramChannel>() {

                    private final UDPMessageAdapter adapter = UDPMessageAdapter.newInstance(config.sessionManager, config.readerIdleTime, config.delimiters);
                    private final MessageDecoderWrapper decoder = new MessageDecoderWrapper(config.decoder);
                    private final MessageEncoderWrapper encoder = new MessageEncoderWrapper(config.encoder);
                    private final DispatcherHandler dispatcher = new DispatcherHandler(config.handlerMapping, config.handlerInterceptor, businessGroup);

                    @Override
                    public void initChannel(NioDatagramChannel channel) {
                        channel.pipeline()
                                .addLast("adapter", adapter)
                                .addLast("decoder", decoder)
                                .addLast("encoder", encoder)
                                .addLast("dispatcher", dispatcher);
                    }
                });
    }
}