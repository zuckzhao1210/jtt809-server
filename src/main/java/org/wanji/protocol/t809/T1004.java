package org.wanji.protocol.t809;

import io.github.yezhihao.protostar.annotation.Message;
import org.wanji.protocol.basic.JTMessage;
import org.wanji.protocol.commons.JT809MainBusiness;

/**
 * @author zhaozhe
 * @description
 * @Date 2024/1/4 15:02
 */

@Message(JT809MainBusiness.主链路注销应答消息)
public class T1004 extends JTMessage {
    /**
     * 空的应答消息，没有数据体
     */
}
