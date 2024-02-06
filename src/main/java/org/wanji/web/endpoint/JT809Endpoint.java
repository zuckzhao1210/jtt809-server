package org.wanji.web.endpoint;


import org.wanji.netmc.core.annotation.Endpoint;
import org.wanji.netmc.core.annotation.Mapping;
import org.wanji.netmc.session.Session;
import org.wanji.protocol.t809.*;
import org.wanji.web.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.wanji.protocol.commons.JT809MainBusiness;


@Endpoint
@Component
public class JT809Endpoint {
    private static final Logger log = LoggerFactory.getLogger(JT809Endpoint.class);

    private LoginService loginService;

    public JT809Endpoint(LoginService loginService) {
        this.loginService = loginService;
    }

    /* 链路管理业务 */

    // 主链路登录请求
    @Mapping(types = JT809MainBusiness.主链路登录请求消息, desc = "主链路登录请求消息")
    public T1002 T1001(T1001 message, Session session) {
        return loginService.login(message, session);
    }


    @Mapping(types = JT809MainBusiness.主链路连接保持请求消息, desc = "主链路连接保持请求消息")
    public T1006 T1005(T1005 message, Session session) {
        return new T1006();
    }

    @Mapping(types = JT809MainBusiness.主链路注销请求消息, desc = "主链路注销请求")
    public T1004 T1003(T1003 message, Session session) {
        return new T1004();
    }
}
