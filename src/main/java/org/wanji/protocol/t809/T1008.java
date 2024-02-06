package org.wanji.protocol.t809;

import io.github.yezhihao.protostar.annotation.Field;
import io.github.yezhihao.protostar.annotation.Message;
import org.wanji.protocol.basic.JTMessage;
import org.wanji.protocol.commons.JT809MainBusiness;

/**
 * Author:  zhaozhe
 * Date:    2023-12-11
 */
@Message(JT809MainBusiness.下级平台主动关闭链路通知消息)
public class T1008 extends JTMessage {

    /**
     * 错误代码
     */
    @Field(length = 1, desc = "错误代码")
    private byte errorCode;

    public byte getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(byte errorCode) {
        this.errorCode = errorCode;
    }

    public enum Result {
        GATEWAY_RESTART((byte)0x00),
        OTHER((byte)0x01);

        private final byte value;
        Result(byte value) {
            this.value = value;
        }
        public byte getValue() {
            return value;
        }
    }
}

