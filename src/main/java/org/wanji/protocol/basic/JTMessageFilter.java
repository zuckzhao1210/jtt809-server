package org.wanji.protocol.basic;


/**
 * 定义端口JTMassageFilter接口
 *  */
public interface JTMessageFilter<T extends JTMessage> {

    // 需要实现doFilter函数
    boolean doFilter(T message);
}