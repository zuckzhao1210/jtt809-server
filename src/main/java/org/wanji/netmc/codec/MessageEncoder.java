package org.wanji.netmc.codec;

import io.netty.buffer.ByteBuf;
import org.wanji.netmc.core.model.Message;
import org.wanji.netmc.session.Session;

/**
 * MessageEncode接口，继承该接口要实现encode方法
 * @param   <T>     消息类型泛型
 */
public interface MessageEncoder <T extends Message>{
    ByteBuf encode(T message, Session session);
}
