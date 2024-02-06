package org.wanji.web.config;

import org.wanji.netmc.NettyConfig;
import org.wanji.netmc.Server;
import org.wanji.netmc.codec.Delimiter;
import org.wanji.netmc.core.HandlerMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.wanji.netmc.session.SessionManager;
import org.wanji.protocol.codec.JTMessageAdapter;
import org.wanji.web.endpoint.JTHandlerInterceptor;

@Order(Integer.MIN_VALUE)
@Configuration
@ConditionalOnProperty(value = "jt-server.jt809.enable", havingValue = "true")
public class JTConfig {
    private final JTMessageAdapter messageAdapter;
    private final HandlerMapping handlerMapping;
    private final JTHandlerInterceptor handlerInterceptor;
    private final SessionManager sessionManager;
    public JTConfig(JTMessageAdapter messageAdapter, HandlerMapping handlerMapping, SessionManager sessionManager, JTHandlerInterceptor handlerInterceptor) {
        this.messageAdapter = messageAdapter;
        this.handlerMapping = handlerMapping;
        this.handlerInterceptor = handlerInterceptor;
        this.sessionManager = sessionManager;
    }

    // 注入tcpServer
    @ConditionalOnProperty(value = "jt-server.jt809.port.tcp")
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server jt809TCPServer(@Value("${jt-server.jt808.port.tcp}") int port) {
        return NettyConfig.custom()     // 通过Builder创建NettyConfig配置

                .setIdleStateTime(900, 0, 0)    //心跳超时(秒),超出15分钟没有收到下级发送的消息，触发空闲事件。
                .setPort(port)
                .setMaxFrameLength(2072)                                            //标识位[2] + 消息头[21] + 消息体[1023 * 2(转义预留)]  + 校验码[1] + 标识位[2]
                .setDelimiters(new Delimiter(new byte[]{0x5b}), new Delimiter(new byte[]{0x5d}))
                .setDecoder(messageAdapter)
                .setEncoder(messageAdapter)
                .setHandlerMapping(handlerMapping)
                .setHandlerInterceptor(handlerInterceptor)
                .setSessionManager(sessionManager)
                .setName("809-TCP")
                .build();
    }

    @ConditionalOnProperty(value = "jt-server.jt809.port.udp")
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server jt809UDPServer(@Value("${jt-server.jt808.port.udp}") int port) {
        return NettyConfig.custom()
                .setPort(port)
                .setDelimiters(new Delimiter(new byte[]{0x5b}), new Delimiter(new byte[]{0x5d}))
                .setDecoder(messageAdapter)
                .setEncoder(messageAdapter)
                .setHandlerMapping(handlerMapping)
                .setHandlerInterceptor(handlerInterceptor)
                .setSessionManager(sessionManager)
                .setName("809-UDP")
                .setEnableUDP(true)
                .build();
        /*
          没有设置 fieldLength
         */
    }


    /* @ConditionalOnProperty(value = "jt-server.alarm-file.enable", havingValue = "true")
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server alarmFileServer(@Value("${jt-server.alarm-file.port}") int port, JTMessageAdapter alarmFileMessageAdapter) {
        return NettyConfig.custom()
                .setPort(port)
                .setMaxFrameLength(2 + 21 + 1023 * 2 + 1 + 2)
                .setLengthField(new LengthField(new byte[]{0x30, 0x31, 0x63, 0x64}, 1024 * 65, 58, 4))
                .setDelimiters(new Delimiter(new byte[]{0x5b, 0x5d}, false))
                .setDecoder(alarmFileMessageAdapter)
                .setEncoder(alarmFileMessageAdapter)
                .setHandlerMapping(handlerMapping)
                // .setHandlerInterceptor(handlerInterceptor)
                .setName("AlarmFile")
                .build();
    } */
}