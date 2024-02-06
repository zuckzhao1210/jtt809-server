package org.wanji.protocol.commons;

public interface JT809MainBusiness {
    /* 主链路 */
    int 主链路登录请求消息 = 0x1001;
    int 主链路登录应答消息 = 0x1002;
    int 主链路注销请求消息 = 0x1003;
    int 主链路注销应答消息 = 0x1004;
    int 主链路连接保持请求消息 = 0x1005;
    int 主链路连接保持应答消息 = 0x1006;
    int 主链路断开通知消息 = 0x1007;
    int 下级平台主动关闭链路通知消息 = 0x1008;
    int 从链路连接请求消息 = 0x9001;
    int 从链路连接应答消息 = 0x9002;
    int 从链路注销请求消息 = 0x9003;
    int 从链路注销应答消息 = 0x9004;
    int 从链路连接保持请求消息 = 0x9005;
    int 从链路连接保持应答消息 = 0x9006;
}
