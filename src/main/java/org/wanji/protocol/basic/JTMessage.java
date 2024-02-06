package org.wanji.protocol.basic;

import io.github.yezhihao.protostar.annotation.Field;
import io.github.yezhihao.protostar.util.ToStringBuilder;
import io.netty.buffer.ByteBuf;
import org.wanji.netmc.core.model.Message;
import org.wanji.netmc.session.Session;
import org.wanji.protocol.commons.MessageId;

import java.beans.Transient;
import java.util.Arrays;

/**
 * 消息类
 */
public class JTMessage implements Message {

    /**
     * 数据长度（包括头标识、数据头、数据体和尾标识）
     */
    @Field(length = 4, desc = "数据长度")
    protected int msgLength;

    /**
     * 报文序列号
     */
    @Field(length = 4, desc = "报文序列号")
    protected int msgSn;

    /**
     * 业务数据类型
     */
    @Field(length = 2, desc = "业务数据类型")
    protected int msgId;

    /**
     * 下级平台接入码，上级平台给下级平台分配的唯一标识
     */
    @Field(length = 4, desc = "下级平台接入码")
    protected int msgGnssCenterId;

    /**
     * 协议版本号标识，上下级之间采用标准的协议版本编号，长度位3个字节
     */
    @Field(length = 3, desc = "协议版本号标识")
    protected byte[] versionFlag;

    /**
     * 报文加密标识位，0标识不加密，1标识加密
     */
    @Field(length = 1, desc = "报文加密标识位")
    protected byte encryptFlag;

    /**
     * 数据加密密钥
     */
    @Field(length = 4, desc = "数据加密的密钥")
    protected int encryptKey;

    // 时间字段，出现在2019的新版协议中，稍后处理
    // @Field(length = 8, desc = "系统时间", version = )
    // protected

    /**
     * 是否通过循环冗余验证
     */
    protected boolean verified = true;

    /**
     * 会话
     */
    protected transient Session session;

    /**
     * 原始报文
     */
    protected transient ByteBuf payload;

    public JTMessage() {
    }

    public JTMessage(int msgId) {
        this.msgId = msgId;
    }

    public JTMessage copyBy(JTMessage that) {
        this.setMsgGnssCenterId(that.getMsgGnssCenterId());
        this.setVersionFlag(that.getVersionFlag());
        return this;
    }

    @Override
    public int getMsgSn() {
        return msgSn;
    }

    public void setMsgSn(int msgSn) {
        this.msgSn = msgSn;
    }

    /* 这里应该优先返回子业务类型ID，对于有些业务类型没有子业务类型，才返回msgId */
    @Override
    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    @Override
    public int getMsgGnssCenterId() {
        return this.msgGnssCenterId;
    }

    public void setMsgGnssCenterId(int msgGnssCenterId) {
        this.msgGnssCenterId = msgGnssCenterId;
    }

    public int getMsgLength() {
        return msgLength;
    }

    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;
    }

    public byte[] getVersionFlag() {
        return versionFlag;
    }

    public void setVersionFlag(byte[] versionFlag) {
        this.versionFlag = versionFlag;
    }

    public byte getEncryptFlag() {
        return encryptFlag;
    }

    public void setEncryptFlag(byte encryptFlag) {
        this.encryptFlag = encryptFlag;
    }

    public int getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(int encryptKey) {
        this.encryptKey = encryptKey;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Transient
    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    @Transient
    public ByteBuf getPayload() {
        return payload;
    }

    public void setPayload(ByteBuf payload) {
        this.payload = payload;
    }

    /**
     * 获取业务类型
     * @return int类型的业务类型编码
     */
    public int reflectMsgId() {
        if (msgId != 0)
            return msgId;
        return reflectMsgId(this.getClass());
    }

    /**
     * 通过反射获取业务类型ID
     * @param clazz 类
     * @return 业务类型ID
     */
    public static int reflectMsgId(Class<?> clazz) {
        io.github.yezhihao.protostar.annotation.Message messageType = clazz.getAnnotation(io.github.yezhihao.protostar.annotation.Message.class);
        if (messageType != null && messageType.value().length > 0)
            return messageType.value()[0];
        return 0;
    }

    public boolean transform() {
        return true;
    }

    public boolean noBuffer() {
        return true;
    }

    /* 数据头22字节+头尾标识2字节+校验码2字节=26个字节 */
    private static final int FIXED_LENGTH = 26;

    public int getBodyLength() {
        return this.msgLength - FIXED_LENGTH;       // 返回数据体长度 = 总的报文长度 - 固定长度26字节
    }

    public void setBodyLength(int bodyLength) {
        this.msgLength = bodyLength + FIXED_LENGTH; // 总的报文长度 = 固定长度 + 数据体长度
    }

    protected StringBuilder toStringHead() {
        final StringBuilder sb = new StringBuilder(768);
        sb.append(MessageId.getName(msgId));
        sb.append('[');
        sb.append("msgGnssCenterId=").append(msgGnssCenterId);
        sb.append(",msgId=").append(msgId);
        sb.append(",versionFlag=").append(Arrays.toString(versionFlag));
        sb.append(",msgSn=").append(msgSn);
        sb.append(",verified=").append(verified);
        sb.append(']');
        sb.append(',');
        return sb;
    }

    @Override
    public String toString() {
        return ToStringBuilder.toString(toStringHead(), this, false, "msgId", "msgGnssCenterId", "versionFlag", "msgSn", "verified");
    }

}
