package org.wanji.netmc.core;

import org.wanji.netmc.core.annotation.Endpoint;
import org.wanji.netmc.util.ClassUtils;

import java.util.List;

/**
 * @author yezhihao
 * <a href="https://gitee.com/yezhihao/jt808-server">...</a>
 */
public class DefaultHandlerMapping extends AbstractHandlerMapping {

    /**
     * 将Endpoint注解的方法注册
     * @param   endpointPackage     注解的方法所在的包
     */
    public DefaultHandlerMapping(String endpointPackage) {
        List<Class> endpointClasses = ClassUtils.getClassList(endpointPackage, Endpoint.class);

        for (Class endpointClass : endpointClasses) {
            try {
                Object bean = endpointClass.getDeclaredConstructor((Class[]) null).newInstance((Object[]) null);
                super.registerHandlers(bean);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}