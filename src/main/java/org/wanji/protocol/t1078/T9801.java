package org.wanji.protocol.t1078;


import io.github.yezhihao.protostar.annotation.Field;
import io.github.yezhihao.protostar.annotation.Message;
import org.wanji.protocol.basic.JTMessage;
import org.wanji.protocol.commons.JT1078;

@Message(JT1078.实时音视频请求消息)
public class T9801 extends JTMessage {
    @Field(length = 21, desc = "车牌号")
    private String vehicleNo;
    @Field(length = 1, desc = "车牌颜色")
    private byte vehicleColor;
    @Field(length = 2, desc = "子业务类型标识")
    private int dataType;
    @Field(length = 4, desc = "后续4个字段长度")
    private int dataLength;
    @Field(length = 1, desc = "逻辑通道号")
    private byte channelId;
    @Field(length = 1, desc = "音视频类型")
    private byte avitemType;
    @Field(length = 64, desc = "时效口令", charset = "ASCII")
    private String authorizeCode;
    @Field(length = 36, desc = "跨域访问字段", charset = "ASCII")
    private String GnssData;

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public byte getVehicleColor() {
        return vehicleColor;
    }

    public void setVehicleColor(byte vehicleColor) {
        this.vehicleColor = vehicleColor;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public byte getChannelId() {
        return channelId;
    }

    public void setChannelId(byte channelId) {
        this.channelId = channelId;
    }

    public byte getAvitemType() {
        return avitemType;
    }

    public void setAvitemType(byte avitemType) {
        this.avitemType = avitemType;
    }

    public String getAuthorizeCode() {
        return authorizeCode;
    }

    public void setAuthorizeCode(String authorizeCode) {
        this.authorizeCode = authorizeCode;
    }

    public String getGnssData() {
        return GnssData;
    }

    public void setGnssData(String gnssData) {
        GnssData = gnssData;
    }
}
