package org.wanji.protocol.t809;

import io.github.yezhihao.protostar.annotation.Field;
import io.github.yezhihao.protostar.annotation.Message;
import org.wanji.protocol.basic.JTMessage;
import org.wanji.protocol.commons.JT809MainBusiness;

/**
 * @author   zhaozhe
 * @date    2023-12-11
 */

@Message(JT809MainBusiness.主链路注销请求消息)
public class T1003 extends JTMessage {

    /**
     * 用户名
     */
    @Field(length = 4, desc = "用户名")
    private int userId;

    /**
     * 密码
     */
    @Field(length = 8, desc = "密码")
    private String password;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
