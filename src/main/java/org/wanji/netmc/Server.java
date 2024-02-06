package org.wanji.netmc;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * @author yezhihao
 * <a href="https://gitee.com/yezhihao/jt808-server">...</a>
 */
public abstract class Server {

    protected static final Logger log = LoggerFactory.getLogger(Server.class);

    protected boolean isRunning;                // 运行标识

    protected final NettyConfig config;         // config实例
    protected EventLoopGroup bossGroup;         // bossGroup
    protected EventLoopGroup workerGroup;       // workerGroup
    protected ExecutorService businessGroup;    // businessGroup

    /**
     * 通过config实例初始化server
     * @param config    配置项实例
     */
    protected Server(NettyConfig config) {
        this.config = config;
    }

    // 抽象的初始化函数
    protected abstract AbstractBootstrap initialize();

    // 启动函数
    public synchronized boolean start() {
        // 判断状态
        if (isRunning) {
            log.warn("==={}已经启动,port:{}===", config.name, config.port);
            return isRunning;
        }
        // 执行初始化函数，返回一个bootstrap
        AbstractBootstrap bootstrap = initialize();
        // 通过bootstrap绑定服务器端口
        ChannelFuture future = bootstrap.bind(config.port).awaitUninterruptibly();
        // 使用netty来监听通道的关闭事件，如果isRunning为true，则调用stop函数
        future.channel().closeFuture().addListener(f -> {
            if (isRunning) stop();
        });

        // future.cause不为null，则意味着启动失败
        if (future.cause() != null)
            log.error("启动失败", future.cause());

        isRunning = future.isSuccess();
        // 检查future的成功状态
        if (isRunning)
            log.warn("==={}启动成功,port:{}===", config.name, config.port);
        return isRunning;
    }

    public synchronized void stop() {
        // 依次关闭 bossgroup、workergroup、businessgroup
        isRunning = false;
        bossGroup.shutdownGracefully();
        if (workerGroup != null)
            workerGroup.shutdownGracefully();
        if (businessGroup != null)
            businessGroup.shutdown();
        log.warn("==={}已经停止,port:{}===", config.name, config.port);
    }
}