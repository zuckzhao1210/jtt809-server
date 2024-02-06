package org.wanji.protocol.t809;

import io.github.yezhihao.protostar.annotation.Field;
import io.github.yezhihao.protostar.annotation.Message;
import org.wanji.protocol.basic.JTMessage;
import org.wanji.protocol.commons.JT809MainBusiness;
/**
 * @author zhaozhe
 * @date 2023/10/13 11:04
 */

@Message(JT809MainBusiness.主链路登录应答消息)
public class T1002 extends JTMessage {

    /**
     * 验证结果 00成功 01IP地址不正确 02接入码不正确 03用户没有注册 04密码错误 05资源紧张 06其他
     */
    @Field(length = 1, desc = "验证结果")
    private byte result;

    /**
     * 验证码
     */
    @Field(length = 4, desc = "校验码")
    private int verifyCode;

    public byte getResult() {
        return result;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    public int getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(int verifyCode) {
        this.verifyCode = verifyCode;
    }

    /**
     * 返回结果的枚举类
     */
    public enum Result{
        SUCCESS((byte)0x00),
        IP_ERROR((byte)0x01),
        GNSSCENTERID_ERROR((byte)0x02),
        USER_UNEXISTING((byte)0x03),
        PASSWORD_ERROR((byte)0x04),
        RESOURCE_SHORTAGE((byte)0x05),
        OTHER_RESULT((byte)0xff);
        private final byte value;
        Result(byte value) {
            this.value = value;
        }
        public byte getValue() {
            return value;
        }
    }
}
