package org.wanji.protocol.commons;

/**
 * 中华人民共和国交通运输行业标准
 * 道路运输车辆卫星定位系统视频通信协议
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
public interface JT1078 {

    int 时效口令上报消息 = 0x1701;
    int 时效口令请求消息 = 0x1702;
    int 时效口令请求应答消息 = 0x9702;
    int 实时音视频请求应答消息 = 0x1801;
    int 主动请求停止实时音视频传输应答消息 = 0x1802;
    int 实时音视频请求消息 = 0x9801;
    int 主动请求停止实时音视频传输消息 = 0x9802;
}
