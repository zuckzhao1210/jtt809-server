package org.wanji.netmc.core;

import org.wanji.netmc.core.annotation.Async;
import org.wanji.netmc.core.annotation.AsyncBatch;
import org.wanji.netmc.core.annotation.Mapping;
import org.wanji.netmc.core.handler.AsyncBatchHandler;
import org.wanji.netmc.core.handler.Handler;
import org.wanji.netmc.core.handler.SimpleHandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息处理映射
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
public abstract class AbstractHandlerMapping implements HandlerMapping {

    private final Map<Object, Handler> handlerMap = new HashMap<>(64);

    /**
     * 将Endpoint中被 @Mapping 标记的方法注册到映射表
     * @param bean
     */
    protected synchronized void registerHandlers(Object bean) {
        Class<?> beanClass = bean.getClass();
        Method[] methods = beanClass.getDeclaredMethods();

        for (Method method : methods) {

            Mapping mapping = method.getAnnotation(Mapping.class);
            if (mapping != null) {

                String desc = mapping.desc();
                int[] types = mapping.types();

                AsyncBatch asyncBatch = method.getAnnotation(AsyncBatch.class);
                Handler handler;

                if (asyncBatch != null) {
                    handler = new AsyncBatchHandler(bean, method, desc, asyncBatch.poolSize(), asyncBatch.maxElements(), asyncBatch.maxWait());

                } else {
                    handler = new SimpleHandler(bean, method, desc, method.isAnnotationPresent(Async.class));
                }

                for (int type : types) {
                    handlerMap.put(type, handler);
                }
            }
        }
    }

    /**
     * 获取处理器
     * @param messageId 业务代码，如果没有子业务类型应该使用主业务类型编码，如果有子业务类型，
     *                  应当使用子业务类型代码了。
     * @return 返回处理器。
     */
    public Handler getHandler(int messageId) {
        return handlerMap.get(messageId);
    }
}