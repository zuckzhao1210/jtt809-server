package org.wanji.netmc.core;

import org.wanji.netmc.core.model.Message;
import org.wanji.netmc.session.Session;

/**
 * 消息拦截器
 * 具体类在Server模块中 JTHandlerInterceptor 实现
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
public interface HandlerInterceptor<T extends Message> {
    /** @return Response 未找到对应的Handle */
    void notSupported(T request, Session session);

    /** @return boolean 调用之前 */
    boolean beforeHandle(T request, Session session);

    /** @return Response 调用之后，返回值为void的 */
    void successful(T request, Session session);

    /** 调用之后，有返回值的 */
    void afterHandle(T request, T response, Session session);

    /** @return Response 调用之后抛出异常的 */
    void exceptional(T request, Session session, Exception e);
}