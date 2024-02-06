package org.wanji.netmc.client;



import org.wanji.netmc.core.annotation.Endpoint;
import org.wanji.netmc.core.annotation.Mapping;
import org.wanji.netmc.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandlerMapping {

    private final Map<Integer, Handler> handlerMap = new HashMap<>(55);

    public HandlerMapping(String... packageNames) {
        for (String packageName : packageNames) {
            initial(packageName);
        }
    }

    private void initial(String packageName) {
        List<Class> handlerClassList = ClassUtils.getClassList(packageName, Endpoint.class);

        for (Class handlerClass : handlerClassList) {
            Method[] methods = handlerClass.getDeclaredMethods();

            for (Method method : methods) {
                if (method.isAnnotationPresent(Mapping.class)) {
                    Mapping annotation = method.getAnnotation(Mapping.class);
                    String desc = annotation.desc();
                    int[] types = annotation.types();
                    Handler value = new Handler(newInstance(handlerClass), method, desc);
                    for (int type : types) {
                        handlerMap.put(type, value);
                    }
                }
            }
        }
    }

    private Object newInstance(Class handlerClass) {
        try {
            return handlerClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Handler getHandler(int messageId) {
        return handlerMap.get(messageId);
    }

}