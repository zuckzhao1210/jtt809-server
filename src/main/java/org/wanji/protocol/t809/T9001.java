package org.wanji.protocol.t809;

import io.github.yezhihao.protostar.annotation.Field;
import io.github.yezhihao.protostar.annotation.Message;
import org.wanji.protocol.basic.JTMessage;
import org.wanji.protocol.commons.JT809MainBusiness;


/**
 * Author:  zhaozhe
 * Date:    2023-12-11
 */
@Message(JT809MainBusiness.从链路连接请求消息)
public class T9001 extends JTMessage {

    /**
     * 校验码
     */
    @Field(length = 4, desc = "校验码")
    private int verifyCode;

    public int getVerifyCode() {
        return this.verifyCode;
    }

    public void setVerifyCode(byte verifyCode) {
        this.verifyCode = verifyCode;
    }

}

