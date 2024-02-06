package org.wanji.netmc;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.wanji.netmc.codec.Delimiter;
import org.wanji.netmc.codec.LengthField;
import org.wanji.netmc.handler.*;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TCPServer继承Server，实现父类的抽象函数 initialize
 * @author yezhihao
 * <a href="https://gitee.com/yezhihao/jt808-server">...</a>
 */
public class TCPServer extends Server {

    /**
     * 父类初始化方法
     * @param config    服务器配置项
     */
    protected TCPServer(NettyConfig config) {
        super(config);
    }

    /**
     * Bootstrap初始化
     */
    protected AbstractBootstrap initialize() {
        // 初始化 netty 环境
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory(config.name, Thread.MAX_PRIORITY));
        workerGroup = new NioEventLoopGroup(config.workerCore, new DefaultThreadFactory(config.name, Thread.MAX_PRIORITY));
        // 如果 businessCore > 0 ，就 new 一个线程池
        if (config.businessCore > 0)
            // config.businessCore核心线程数量，  1L，TimeUnit.SECONDS 标识非核心线程 1s 被终止，
            businessGroup = new ThreadPoolExecutor(config.businessCore, config.businessCore, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new DefaultThreadFactory(config.name + "-B", true, Thread.NORM_PRIORITY));
        return new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(NioChannelOption.SO_REUSEADDR, true)
                .option(NioChannelOption.SO_BACKLOG, 1024)
                .childOption(NioChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    private final TCPMessageAdapter adapter = new TCPMessageAdapter(config.sessionManager);
                    private final MessageDecoderWrapper decoder = new MessageDecoderWrapper(config.decoder);
                    private final MessageEncoderWrapper encoder = new MessageEncoderWrapper(config.encoder);        // 继承 ChannelOutboundHandlerAdapter，其他三个为 ChannelInboundHandlerAdapter
                    private final DispatcherHandler dispatcher = new DispatcherHandler(config.handlerMapping, config.handlerInterceptor, businessGroup);

                    @Override
                    public void initChannel(NioSocketChannel channel) {
                        channel.pipeline()
                                .addLast(new IdleStateHandler(config.readerIdleTime, config.writerIdleTime, config.allIdleTime))
                                .addLast("frameDecoder", frameDecoder())        // 分包处理流程
                                .addLast("adapter", adapter)                    // 会话管理部分
                                .addLast("decoder", decoder)                    // Decoder
                                .addLast("encoder", encoder)                    // Encoder
                                .addLast("dispatcher", dispatcher);             // 业务分发处理流程
                    }
                });
    }


    /**
     *  解码
     * @return ByteToMessageDecoder
     */
    private ByteToMessageDecoder frameDecoder() {
        // JTConfig中没有设置 lengthField，一般直接 return
        return getByteToMessageDecoder(config.lengthField, config.delimiters, config.maxFrameLength);
    }

    /**
     * 分包处理函数
     * @param lengthField   长度属性
     * @param delimiters    标识符数组
     * @param maxFrameLength    最大报文长度
     * @return  一个ByteToMessageDecoder
     */
    public static ByteToMessageDecoder getByteToMessageDecoder(LengthField lengthField, Delimiter[] delimiters, Integer maxFrameLength) {
        if (lengthField != null) {
            if (delimiters != null) {
                return new LengthFieldAndDelimiterFrameDecoder(maxFrameLength, lengthField, delimiters);
            } else {
                return new LengthFieldBasedFrameDecoder(maxFrameLength,
                        lengthField.lengthFieldOffset, lengthField.lengthFieldLength,
                        lengthField.lengthAdjustment, lengthField.initialBytesToStrip);
            }
        }
        return new DelimiterBasedFrameDecoder(maxFrameLength, delimiters);
    }
}