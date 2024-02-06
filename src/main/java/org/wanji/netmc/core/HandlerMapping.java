package org.wanji.netmc.core;

        import org.wanji.netmc.core.handler.Handler;

/**
 * 消息映射接口
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
public interface HandlerMapping {

    Handler getHandler(int messageId);

}