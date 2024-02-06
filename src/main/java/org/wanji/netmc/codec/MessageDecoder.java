package org.wanji.netmc.codec;

import io.netty.buffer.ByteBuf;
import org.wanji.netmc.core.model.Message;
import org.wanji.netmc.session.Session;

/**
 * MessageDecoder接口，实现该接口要实现 decode 方法
 * @param   <T>     消息类泛型
 */
public interface MessageDecoder<T extends Message> {
    T decode(ByteBuf buf, Session session);
}
