package org.wanji.protocol.t809;

import io.github.yezhihao.protostar.annotation.Field;
import io.github.yezhihao.protostar.annotation.Message;
import org.wanji.protocol.basic.JTMessage;
import org.wanji.protocol.commons.JT809MainBusiness;

/**
 * @author zhaozhe
 * @date 2023/10/11 17:35
 */
@Message(JT809MainBusiness.主链路登录请求消息)
public class T1001 extends JTMessage {

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
    // @Field(length = 32, desc = "下级平台接入码")
    // private int msgGnssCenterId;

    /**
     * 下级平台提供的从链路IP地址
     */
    @Field(length = 32, desc = "下级平台提供的从链路IP地址")
    private String downLinkIp;

    /**
     * 下级平台提供的从链路端口号
     */
    @Field(length = 2, desc = "下级平台提供的从链路端口号")
    private int downLinkPort;

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

    @Override
    public int getMsgGnssCenterId() {
        return msgGnssCenterId;
    }

    public void setMsgGnssCenterId(int msgGnssCenterId) {
        this.msgGnssCenterId = msgGnssCenterId;
    }

    public String getDownLinkIp() {
        return downLinkIp;
    }

    public void setDownLinkIp(String downLinkIp) {
        this.downLinkIp = downLinkIp;
    }

    public int getDownLinkPort() {
        return downLinkPort;
    }

    public void setDownLinkPort(int downLinkPort) {
        this.downLinkPort = downLinkPort;
    }
}
