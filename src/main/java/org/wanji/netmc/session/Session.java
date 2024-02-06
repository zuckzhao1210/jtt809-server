package org.wanji.netmc.session;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanji.netmc.core.model.Message;
import org.wanji.netmc.core.model.Response;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

/**
 * @author zhaozhe
 * @date 2023/10/11 18:03
 */
public class Session {

    private static final Logger log = LoggerFactory.getLogger(Session.class);

    private final boolean udp;
    private final Function<Session, Boolean> remover;
    protected final Channel channel;
    private final SessionManager sessionManager;
    private final InetSocketAddress remoteAddress;
    private final String remoteAddressStr;
    private final long creationTime;
    private long lastAccessedTime;

    // 记录各种属性
    private final Map<Object, Object> attributes;

    private int sessionId;

    /* 上级平台给下级平台分配的唯一标号 */
    private int msgGnssCenterId;

    private final AtomicInteger serialNo = new AtomicInteger(0);

    private BiConsumer<Session, Message> requestInterceptor = (session, message) -> {
    };
    private BiConsumer<Session, Message> responseInterceptor = (session, message) -> {
    };

    public Session(SessionManager sessionManager,
                   Channel channel,
                   InetSocketAddress remoteAddress,
                   Function<Session, Boolean> remover,
                   boolean udp) {
        this.channel = channel;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = creationTime;
        this.sessionManager = sessionManager;
        this.remoteAddress = remoteAddress;
        this.remoteAddressStr = remoteAddress.toString();
        this.remover = remover;
        this.udp = udp;
        // 如果sessionManager非空且
        if (sessionManager != null && sessionManager.getSessionKeyClass() != null)
            this.attributes = new EnumMap(sessionManager.getSessionKeyClass());
        else
            this.attributes = new TreeMap<>();
    }

    /**
     * 注册到SessionManager
     */
    public void register(Message message) {
        register(message.getMsgGnssCenterId(), message);
    }

    public void register(int sessionId, Message message) {
        if (sessionId == 0)
            throw new NullPointerException("sessionId not null");
        this.sessionId = sessionId;
        this.msgGnssCenterId = message.getMsgGnssCenterId();
        if (sessionManager != null)
            sessionManager.add(this);
        log.info("<<<<< Registered{}", this);
    }

    public boolean isRegistered() {
        return sessionId != 0;
    }

    public int getId() {
        return sessionId;
    }

    public int getMsgGnssCenterId() {
        return msgGnssCenterId;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public long access() {
        lastAccessedTime = System.currentTimeMillis();
        return lastAccessedTime;
    }

    public Collection<Object> getAttributeNames() {
        return attributes.keySet();
    }

    public Map<Object, Object> getAttributes() {
        return attributes;
    }

    public Object getAttribute(Object name) {
        return attributes.get(name);
    }

    public void setAttribute(Object name, Object value) {
        attributes.put(name, value);
    }

    public Object removeAttribute(Object name) {
        return attributes.remove(name);
    }

    public Object getOfflineCache(int clientId) {
        if (sessionManager != null)
            return sessionManager.getOfflineCache(clientId);
        return null;
    }

    public void setOfflineCache(int clientId, Object value) {
        if (sessionManager != null)
            sessionManager.setOfflineCache(clientId, value);
    }

    public InetSocketAddress remoteAddress() {
        return remoteAddress;
    }

    public void requestInterceptor(BiConsumer<Session, Message> requestInterceptor) {
        if (requestInterceptor != null)
            this.requestInterceptor = requestInterceptor;
    }

    public void responseInterceptor(BiConsumer<Session, Message> responseInterceptor) {
        if (responseInterceptor != null)
            this.responseInterceptor = responseInterceptor;
    }

    private static final IntUnaryOperator UNARY_OPERATOR = prev -> prev >= 0xFFFF ? 0 : prev + 1;

    public int nextSerialNo() {
        return serialNo.getAndUpdate(UNARY_OPERATOR);
    }

    public void invalidate() {
        if (isRegistered() && sessionManager != null)
            sessionManager.remove(this);
        remover.apply(this);
    }

    public String getRemoteAddressStr() {
        return remoteAddressStr;
    }

    public boolean isUdp() {
        return udp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(50);
        sb.append(remoteAddressStr);
        sb.append('/').append(sessionId);
        if (!Objects.equals(sessionId, msgGnssCenterId))
            sb.append('/').append(msgGnssCenterId);
        return sb.toString();
    }

    private final Map<String, MonoSink> topicSubscribers = new HashMap<>();

    private static final Mono Rejected = Mono.error(new RejectedExecutionException("客户端暂未响应，请勿重复发送"));

    /**
     * 异步发送通知类消息
     * 同步发送 mono.block()
     * 订阅回调 mono.doOnSuccess({处理成功}).doOnError({处理异常}).subscribe()开始订阅
     */
    public Mono<Void> notify(Message message) {
        requestInterceptor.accept(this, message);
        Packet packet = Packet.of(this, message);
        // 使用Mono.create()方法来创建一个新的Mono对象，该对象用于表示异步操作的结果。在内部，它执行以下操作：
        //             1. channel.writeAndFlush(packet)：这是异步写入packet到channel的操作。这可能是一个网络通信操作，它会异步完成。
        //             2. .addListener(future -> {...})：在写入完成后，通过添加一个监听器，您可以在future中处理写入操作的结果。
        //                 如果写入成功，将调用sink.success()来表示成功。如果写入失败，将调用sink.error(future.cause())来表示失败。
        //             3. sink.success()：如果写入操作成功，这将触发Mono的成功结果。
        //                sink.error(future.cause())：如果写入操作失败，这将触发Mono的错误结果，并将future中的错误作为参数传递给sink.error()。
        return Mono.create(sink -> channel.writeAndFlush(packet).addListener(future -> {
            if (future.isSuccess()) {
                sink.success();
            } else {
                sink.error(future.cause());
            }
        }));
    }

    public Mono<Void> notify(ByteBuf message) {
        Packet packet = Packet.of(this, message);
        return Mono.create(sink -> channel.writeAndFlush(packet).addListener(future -> {
            if (future.isSuccess()) {
                sink.success();
            } else {
                sink.error(future.cause());
            }
        }));
    }

    /**
     * 异步发送消息，接收响应
     * 同步接收 mono.block()
     * 订阅回调 mono.doOnSuccess({处理成功}).doOnError({处理异常}).subscribe()开始订阅
     */
    public <T> Mono<T> request(Message request, Class<T> responseClass) {
        requestInterceptor.accept(this, request);
        String key = requestKey(request, responseClass);
        Mono<T> receive = this.subscribe(key);
        if (receive == null) {
            return Rejected;
        }

        Packet packet = Packet.of(this, request);
        return Mono.create(sink -> channel.writeAndFlush(packet).addListener(future -> {
            if (future.isSuccess()) {
                sink.success(future);
            } else {
                sink.error(future.cause());
            }
        })).then(receive).doFinally(signal -> unsubscribe(key));
    }

    /**
     * 消息响应
     */
    public boolean response(Message message) {
        responseInterceptor.accept(this, message);
        MonoSink<Message> sink = topicSubscribers.get(responseKey(message));
        if (sink != null) {
            sink.success(message);
            return true;
        }
        return false;
    }

    private Mono subscribe(String key) {
        synchronized (topicSubscribers) {
            if (!topicSubscribers.containsKey(key)) return Mono.create(sink -> topicSubscribers.put(key, sink));
        }
        return null;
    }

    private void unsubscribe(String key) {
        topicSubscribers.remove(key);
    }

    private static String requestKey(Message request, Class responseClass) {
        String className = responseClass.getName();
        if (Response.class.isAssignableFrom(responseClass)) {
            int serialNo = request.getMsgSn();
            return new StringBuilder(34).append(className).append('.').append(serialNo).toString();
        }
        return className;
    }

    private static String responseKey(Object response) {
        String className = response.getClass().getName();
        if (response instanceof Response) {
            int serialNo = ((Response) response).getMsgSn();
            return new StringBuilder(34).append(className).append('.').append(serialNo).toString();
        }
        return className;
    }
}
