package org.wanji.netmc.session;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


/**
 * @author zhaozhe
 */
public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    /**
     * sessionMap，客户端唯一ID为Key，Session为value
     */
    private final ConcurrentHashMap<Integer, Session> sessionMap;

    /**
     * 缓冲Cache，客户端唯一ID为Key
     */
    private final Cache<Integer, Object> offlineCache;

    /**
     * sessionListener
     */
    private final SessionListener sessionListener;

    /**
     * 表示一个Enum的子类之类的Class对象
     */
    private final Class<? extends Enum> sessionKeyClass;

    /**
     * 无参构造方法
     */
    public SessionManager() {
        this(null, null);
    }

    /**
     * 有参构造方法
     * @param sessionListener sessionListener
     */
    public SessionManager(SessionListener sessionListener) {
        this(null, sessionListener);
    }

    /**
     * 有参构造方法
     * @param sessionKeyClass sessionKeyClass
     * @param sessionListener sessionListener
     */
    public SessionManager(Class<? extends Enum> sessionKeyClass, SessionListener sessionListener) {
        this.sessionMap = new ConcurrentHashMap<>();
        this.offlineCache = Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
        this.sessionKeyClass = sessionKeyClass;
        this.sessionListener = sessionListener;
    }

    public Session get(String sessionId) {
        return sessionMap.get(sessionId);
    }

    @Deprecated
    public Collection<Session> all() {
        return values();
    }

    public Collection<Session> values() {
        return sessionMap.values();
    }

    /**
     *
     * @param channel 通道
     * @return  Session
     */
    public Session newInstance(Channel channel) {
        InetSocketAddress sender = (InetSocketAddress) channel.remoteAddress();         // 远程地址
        Session session = new Session(this, channel, sender, s -> {
            channel.close();
            return true;
        }, false);
        if (sessionListener != null)
            try {
                sessionListener.sessionCreated(session);
            } catch (Exception e) {
                log.error("sessionCreated", e);
            }
        return session;
    }

    public Session newInstance(Channel channel, InetSocketAddress sender, Function<Session, Boolean> remover) {
        Session session = new Session(this, channel, sender, remover, true);
        if (sessionListener != null)
            try {
                sessionListener.sessionCreated(session);
            } catch (Exception e) {
                log.error("sessionCreated", e);
            }
        return session;
    }

    protected void remove(Session session) {
        boolean remove = sessionMap.remove(session.getId(), session);
        if (remove && sessionListener != null)
            try {
                sessionListener.sessionDestroyed(session);
            } catch (Exception e) {
                log.error("sessionDestroyed", e);
            }
    }

    protected void add(Session newSession) {
        Session oldSession = sessionMap.put(newSession.getId(), newSession);
        if (sessionListener != null)
            try {
                sessionListener.sessionRegistered(newSession);
            } catch (Exception e) {
                log.error("sessionRegistered", e);
            }
    }

    public void setOfflineCache(int platformId, Object value) {
        offlineCache.put(platformId, value);
    }

    public Object getOfflineCache(int platformId) {
        return offlineCache.getIfPresent(platformId);
    }

    public Class<? extends Enum> getSessionKeyClass() {
        return sessionKeyClass;
    }
}