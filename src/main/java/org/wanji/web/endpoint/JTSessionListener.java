package org.wanji.web.endpoint;

import org.wanji.netmc.core.model.Message;
import org.wanji.netmc.session.Session;
import org.wanji.netmc.session.SessionListener;
import org.wanji.protocol.basic.JTMessage;
import org.wanji.web.model.entity.PlatformDO;
import org.wanji.web.model.enums.SessionKey;
import java.util.function.BiConsumer;

public class JTSessionListener implements SessionListener {

    /**
     * 下行消息拦截器
     */
    private static final BiConsumer<Session, Message> requestInterceptor = (session, message) -> {
        JTMessage request = (JTMessage) message;
        request.setMsgGnssCenterId(session.getMsgGnssCenterId());
        request.setMsgSn(session.nextSerialNo());
        // 如果通过getMsgId获取不到消息的类型ID，就通过反射的方法通过类的注解Message获取
        if (request.getMsgId() == 0) {
            request.setMsgId(request.reflectMsgId());
        }

        // 获取下级平台
        PlatformDO platformDO = SessionKey.getPlatform(session);
        if (platformDO != null) {
            // 获取协议版本号
            int protocolVersion = platformDO.getProtocolVersion();
            // if (protocolVersion > 0) {
            //     request.setVersion(true);
            //     request.setVersionFlag(protocolVersion.to);
            // }
        }
    };

    /**
     * 设备连接
     */
    @Override
    public void sessionCreated(Session session) {
        session.requestInterceptor(requestInterceptor);
    }

    /**
     * 设备注册
     */
    @Override
    public void sessionRegistered(Session session) {
    }

    /**
     * 设备离线
     */
    @Override
    public void sessionDestroyed(Session session) {
    }
}