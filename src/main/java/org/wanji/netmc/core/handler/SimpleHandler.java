package org.wanji.netmc.core.handler;

import org.wanji.netmc.core.model.Message;
import org.wanji.netmc.session.Session;

import java.lang.reflect.Method;

/**
 * 同步处理
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
public class SimpleHandler extends Handler {

    public SimpleHandler(Object actionClass, Method actionMethod, String desc, boolean async) {
        super(actionClass, actionMethod, desc, async);
    }

    public <T extends Message> T invoke(T request, Session session) throws Exception {
        return super.invoke(request, session);
    }
}