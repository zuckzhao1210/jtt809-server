package org.wanji.web.config;

import org.wanji.netmc.core.HandlerMapping;
import org.wanji.netmc.core.SpringHandlerMapping;
import io.github.yezhihao.protostar.SchemaManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wanji.netmc.session.SessionListener;
import org.wanji.netmc.session.SessionManager;
import org.wanji.protocol.codec.JTMessageAdapter;
import org.wanji.protocol.codec.JTMessageDecoder;
import org.wanji.protocol.codec.JTMessageEncoder;
import org.wanji.web.endpoint.JTHandlerInterceptor;
import org.wanji.web.endpoint.JTSessionListener;
import org.wanji.web.model.enums.SessionKey;

/**
 * @author zhaozhe
 * &#064;date  2023/10/16 14:30
 */

@Configuration
public class JTBeanConfig {

    /* 使用SpringHandlerMapping类注册 @Endpoint 注解的控制器类 */
    @Bean
    public HandlerMapping handlerMapping() {
        return new SpringHandlerMapping();
    }

    @Bean
    public JTHandlerInterceptor handlerInterceptor() {
        return new JTHandlerInterceptor();
    }

    /* 消息监听器 */
    @Bean
    public SessionListener sessionListener() {
        return new JTSessionListener();
    }

    /* 主链路会话管理器 */
    @Bean
    public SessionManager sessionManager(SessionListener sessionListener) {
        return new SessionManager(SessionKey.class, sessionListener);
    }

    /* 从链路会话管理器 */
    @Bean
    public SessionManager slaveSessionManager(SessionListener sessionListener) {
        return new SessionManager(SessionKey.class, sessionListener);
    }

    /* 协议的序列化管理器 */
    @Bean
    public SchemaManager schemaManager() {
        return new SchemaManager("org.wanji.protocol");
    }

    /* encoder 和 decoder */
    @Bean
    public JTMessageAdapter messageAdapter(SchemaManager schemaManager) {
        JTMessageEncoder encoder = new JTMessageEncoder(schemaManager);
        JTMessageDecoder decoder = new JTMessageDecoder(schemaManager);
        return new WebLogAdapter(encoder, decoder);
    }

    /* @Bean
    public JTMessageAdapter alarmFileMessageAdapter(SchemaManager schemaManager) {
        JTMessageEncoder encoder = new JTMessageEncoder(schemaManager);
        DataFrameMessageDecoder decoder = new DataFrameMessageDecoder(schemaManager, new byte[]{0x30, 0x31, 0x63, 0x64});
        return new WebLogAdapter(encoder, decoder);
    } */

    /* //809没有定义分包的情况
    @Bean
    public MultiPacketDecoder multiPacketDecoder(SchemaManager schemaManager) {
        return new MultiPacketDecoder(schemaManager);
    } */

}