package org.wanji.web.endpoint;

import org.wanji.protocol.basic.JTMessage;
import org.wanji.netmc.session.Session;
import org.wanji.netmc.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.wanji.commons.model.APIException;
import org.wanji.commons.model.APIResult;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
@Component
public class MessageManager {
    private static final Logger log = LoggerFactory.getLogger(MessageManager.class);
    /**
     * reactor.core.publisher.Mono 是 Project Reactor 框架中的一个类，
     * 用于表示一个包含单个元素或空的响应式流。
     * Project Reactor是一种响应式编程库，用于处理异步和事件驱动的编程。
     * Mono 可以包含一个值、错误或者表示空（没有值）。
     * 它是响应式流的一种，类似于Java 8中的java.util.Optional，但具有更多的操作和功能，使其适用于响应式编程的场景。
     */
    private static final Mono<Void> NEVER = Mono.never();
    private static final Mono OFFLINE_EXCEPTION = Mono.error(new APIException(4000, "离线的客户端（请检查设备是否注册或者鉴权）"));
    private static final Mono OFFLINE_RESULT = Mono.just(new APIResult<>(4000, "离线的客户端（请检查设备是否注册或者鉴权）"));
    private static final Mono SENDFAIL_RESULT = Mono.just(new APIResult<>(4001, "消息发送失败"));
    private static final Mono TIMEOUT_RESULT = Mono.just(new APIResult<>(4002, "消息发送成功,客户端响应超时（至于设备为什么不应答，请联系设备厂商）"));

    private SessionManager sessionManager;

    public MessageManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /* 验证session是否为null，如果为null，则返回异常 */
    public Mono<Void> notifyR(String sessionId, JTMessage request) {
        Session session = sessionManager.get(sessionId);
        if (session == null)
            return OFFLINE_EXCEPTION;
        // 返回
        return session.notify(request);
    }

    /*  */
    public Mono<Void> notify(String sessionId, JTMessage request) {
        Session session = sessionManager.get(sessionId);
        if (session == null)
            return NEVER;

        return session.notify(request);
    }

    public <T> Mono<APIResult<T>> requestR(String sessionId, JTMessage request, Class<T> responseClass) {
        Session session = sessionManager.get(sessionId);
        if (session == null)
            return OFFLINE_RESULT;

        return session.request(request, responseClass)
                .map(message -> APIResult.ok(message))
                .timeout(Duration.ofSeconds(10), TIMEOUT_RESULT)
                .onErrorResume(e -> {
                    log.warn("消息发送失败", e);
                    return SENDFAIL_RESULT;
                });
    }

    /**
     * 处理请求消息体，获取session，然后发送请求消息体。
     */
    public <T> Mono<APIResult<T>> requestR(JTMessage request, Class<T> responseClass) {
        // 获取会话
        Session session = sessionManager.get(Integer.toString(request.getMsgGnssCenterId()));
        if (session == null)
            return OFFLINE_RESULT;
        // 发送消息体
        return session.request(request, responseClass)
                .map(message -> APIResult.ok(message))
                .timeout(Duration.ofSeconds(10), TIMEOUT_RESULT)
                .onErrorResume(e -> {
                    log.warn("消息发送失败", e);
                    return SENDFAIL_RESULT;
                });
    }

    public <T> Mono<T> request(String sessionId, JTMessage request, Class<T> responseClass, long timeout) {
        return request(sessionId, request, responseClass).timeout(Duration.ofMillis(timeout));
    }

    public <T> Mono<T> request(String sessionId, JTMessage request, Class<T> responseClass) {
        Session session = sessionManager.get(sessionId);
        if (session == null)
            return OFFLINE_EXCEPTION;

        return session.request(request, responseClass);
    }
}