package org.wanji.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.wanji.netmc.session.Session;
import org.wanji.protocol.t809.T1001;
import org.wanji.protocol.t809.T1002;


import java.util.Properties;

/**
 * @author zhaozhe
 * @date 2023/10/12 10:54
 */

@Service
public class LoginService {

    // private final JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(LoginService.class);
    private static Properties properties = new Properties();
    public LoginService() {
        // this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 先按配置文件将用户名密码写死，后续再改
     * @param message   报文
     * @return T1002.Result 结果码
     */
    public static int verify(T1001 message) {
        String username = Integer.toString(message.getUserId());
        String password = message.getPassword();
        if (password == null) {
            log.warn("===下级平台 {} 提供的用户名密码或为空", message.getMsgGnssCenterId());
            return T1002.Result.USER_UNEXISTING.getValue();
        } else if ((username.equals(properties.getProperty("jtt809-server.userid"))) &&
                   password.equals(properties.getProperty("jtt809-server.password"))) {
            log.info("===下级平台 {} 下级平台登录成功", message.getMsgGnssCenterId());
            return T1002.Result.SUCCESS.getValue();
        } else {
            log.info("===下级平台 {} 下级平台用户名或密码错误", message.getMsgGnssCenterId());
            return T1002.Result.PASSWORD_ERROR.getValue();
        }
    }

    /**
     * 登录服务
     * @param message
     * @param session
     * @return
     */
    public T1002 login(T1001 message, Session session) {
        // 1. 验证登录
        int verifyResult = verify(message);
        // 2. 组装响应消息 0x1002 消息
        T1002 resultMsg = new T1002();
        resultMsg.setResult((byte)verifyResult);
        resultMsg.setVerifyCode(99999999);              // 验证码后续加入验证码生成方法。
        return resultMsg;
    }
}
