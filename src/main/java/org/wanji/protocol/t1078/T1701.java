package org.wanji.protocol.t1078;

import io.github.yezhihao.protostar.annotation.Field;
import io.github.yezhihao.protostar.annotation.Message;
import org.wanji.protocol.basic.JTMessage;
import org.wanji.protocol.commons.JT1078;

@Message(JT1078.时效口令上报消息)
public class T1701 extends JTMessage {
    @Field(length = 2, desc = "子业务类型标识")
    private int dataType;
    @Field(length = 11, desc = "企业视频监控平台唯一编码")
    private String plateformId;
    @Field(length = 64, desc = "归属地区政府平台使用的时效口令")
    private String authorizeCode1;
    @Field(length = 64, desc = "跨域地区政府平台使用的时效口令")
    private String authorizeCode2;

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public String getPlateformId() {
        return plateformId;
    }

    public void setPlateformId(String plateformId) {
        this.plateformId = plateformId;
    }

    public String getAuthorizeCode1() {
        return authorizeCode1;
    }

    public void setAuthorizeCode1(String authorizeCode1) {
        this.authorizeCode1 = authorizeCode1;
    }

    public String getAuthorizeCode2() {
        return authorizeCode2;
    }

    public void setAuthorizeCode2(String authorizeCode2) {
        this.authorizeCode2 = authorizeCode2;
    }
}
