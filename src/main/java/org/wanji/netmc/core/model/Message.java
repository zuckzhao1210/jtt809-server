package org.wanji.netmc.core.model;

import java.io.Serializable;

/**
 * Message接口定义了协议数据的数据头各字段，因数据头是公用格式，所有的消息都应该带上数据头。
 * JTMessage是Message的实现类，继承了数据头的格式。
 */
public interface Message extends Serializable {

    /* 数据长度（包括头标识、数据头、数据体和尾标识） */
    int getMsgLength();

    /* 报文序列号 */
    int getMsgSn();

    /* 业务数据类型 */
    int getMsgId();

    /* 下级平台接入码 */
    int getMsgGnssCenterId();

    /* 协议版本号标识 */
    byte[] getVersionFlag();

    /* 数据加密密钥 */
    int getEncryptKey();
}