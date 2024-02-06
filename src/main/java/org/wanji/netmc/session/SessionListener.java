package org.wanji.netmc.session;

/**
 * 会话监听器的接口，实现该接口，需要实现会话连接创建、客户端注册、客户端注销三个函数
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
public interface SessionListener {

    /** 客户端建立连接 */
    default void sessionCreated(Session session) {
    }

    /** 客户端完成注册或鉴权 */
    default void sessionRegistered(Session session) {
    }

    /** 客户端注销或离线 */
    default void sessionDestroyed(Session session) {
    }
}