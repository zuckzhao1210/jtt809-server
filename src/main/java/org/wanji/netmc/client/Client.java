package org.wanji.netmc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanji.netmc.Server;

/**
 * @author zhaozhe
 * @date 2023/10/17 10:20
 */
public abstract class Client {

    protected static final Logger log = LoggerFactory.getLogger(Server.class);
    protected boolean isRunning;

    protected final ClientConfig config;

    // protected EventLoopGroup bossGroup;
    protected EventLoopGroup workerGroup;

    protected Channel channel;

    protected Client(ClientConfig config) { this.config = config; }

    // 抽象的bootstrap初始化方法
    protected abstract Bootstrap initialize();

    // 启动客户端
    public synchronized boolean start() throws InterruptedException {
        if (isRunning) {
            // log.warn("===上级平台从链路客户端 {} 已经启动,port:{}===", config.name, config.port);
            return isRunning;
        }
        // 调用初始化方法
        Bootstrap bootstrap = initialize();
        // 链接IP和端口
        ChannelFuture future = bootstrap.connect(config.ip, config.port).sync();
        this.channel = future.channel();
        this.channel.closeFuture();

        future.channel().closeFuture().addListener(f -> {
            if (isRunning) stop();
        });
        if (future.cause() != null)
            log.error("启动失败", future.cause());

        // if (isRunning = future.isSuccess())
        // log.warn("==={}启动成功,port:{}===", config.name, config.port);
        return isRunning;
    }

    // 关闭客户端
    public synchronized void stop() {
        isRunning = false;
        if (workerGroup != null)
            workerGroup.shutdownGracefully();
        // log.warn("==={}已经停止,port:{}===", config.name, config.port);
    }

}