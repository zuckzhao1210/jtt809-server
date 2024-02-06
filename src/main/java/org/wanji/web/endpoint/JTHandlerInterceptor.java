package org.wanji.web.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanji.netmc.core.HandlerInterceptor;
import org.wanji.netmc.session.Session;
import org.wanji.protocol.basic.JTMessage;
import org.wanji.protocol.commons.JT809MainBusiness;


public class JTHandlerInterceptor implements HandlerInterceptor<JTMessage> {

    private static final Logger log = LoggerFactory.getLogger(JTHandlerInterceptor.class);

    /**
     * 未找到对应的Handle，809没有应答未识别数据。
     * @return
     */
    @Override
    public void notSupported(JTMessage request, Session session) {
        log.info("{}\n<<<<-未识别的消息{}\n>>>>", session, request);
    }


    /**
     * 调用之后，返回值为void的
     */
    @Override
    public void successful(JTMessage request, Session session) {
        log.warn(session + "\n<<<<-" + request + "\n>>>>-"  + '\n');
    }

    /**
     * 调用之后抛出异常的
     */
    @Override
    public void exceptional(JTMessage request, Session session, Exception e) {
        log.warn(session + "\n<<<<-" + request + "\n>>>>-"  + '\n', e);
    }

    /** 调用之前 */
    @Override
    public boolean beforeHandle(JTMessage request, Session session) {
        int messageId = request.getMsgId();
        if (messageId == JT809MainBusiness.主链路登录请求消息)
            return true;
        boolean transform = request.transform();
        // if (messageId == JT808.位置信息汇报) {
        //     DeviceDO device = SessionKey.getDevice(session);
        //     if (device != null)
        //         device.setLocation((T0200) request);
        //     return transform;
        // }
        if (!session.isRegistered()) {
            log.info("{}未注册的下级平台<<<<-{}", session, request);
            return true;
        }
        return true;
    }

    /**
     * 调用之后设置消息业务类型msgId和消息序列号msgSn
     * @param request   请求消息
     * @param response  回复消息
     * @param session   会话
     */
    @Override
    public void afterHandle(JTMessage request, JTMessage response, Session session) {
        if (response != null) {
            response.copyBy(request);
            response.setMsgSn(session.nextSerialNo());

            if (response.getMsgId() == 0) {
                response.setMsgId(response.reflectMsgId());
            }
        }
    }
}