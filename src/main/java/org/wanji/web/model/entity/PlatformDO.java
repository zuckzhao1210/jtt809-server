package org.wanji.web.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

public class PlatformDO {

    @Schema(description = "下级平台id")
    private int plateformId;
    @Schema(description = "下级平台主链路IP地址")
    private String primaryLinkIp;
    @Schema(description = "下级平台主链路端口")
    protected int primaryLinkPort;
    @Schema(description = "下级平台从链路IP地址")
    protected String slaveLinkIp;
    @Schema(description = "下级平台从链路端口")
    private int slaveLinkPort;
    @Schema(description = "协议版本号")
    private int protocolVersion;

    // @Schema(description = "实时状态")
    // private T0200 location;

    public PlatformDO() {
    }

    public int getPlateformId() {
        return plateformId;
    }

    public void setPlateformId(int plateformId) {
        this.plateformId = plateformId;
    }

    public String getPrimaryLinkIp() {
        return primaryLinkIp;
    }

    public void setPrimaryLinkIp(String primaryLinkIp) {
        this.primaryLinkIp = primaryLinkIp;
    }

    public int getPrimaryLinkPort() {
        return primaryLinkPort;
    }

    public void setPrimaryLinkPort(int primaryLinkPort) {
        this.primaryLinkPort = primaryLinkPort;
    }

    public String getSlaveLinkIp() {
        return slaveLinkIp;
    }

    public void setSlaveLinkIp(String slaveLinkIp) {
        this.slaveLinkIp = slaveLinkIp;
    }

    public int getSlaveLinkPort() {
        return slaveLinkPort;
    }

    public void setSlaveLinkPort(int slaveLinkPort) {
        this.slaveLinkPort = slaveLinkPort;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

/*     public T0200 getLocation() {
        return location;
    }

    public void setLocation(T0200 location) {
        this.location = location;
    } */

    /*  */
    public PlatformDO plateformId(int platformId) {
        this.plateformId = platformId;
        return this;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        PlatformDO other = (PlatformDO) that;
        return Objects.equals(this.plateformId, other.plateformId);
    }

/*
    @Override
    public int hashCode() {
        return ((deviceId == null) ? 0 : deviceId.hashCode());
    }
*/

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        sb.append("PlateForm{platformId=").append(plateformId);
        sb.append(", primaryLinkIp=").append(primaryLinkIp);
        sb.append(", primaryLinkPort=").append(primaryLinkPort);
        sb.append(", slaveLinkIp=").append(slaveLinkIp);
        sb.append(", slaveLinkPort=").append(slaveLinkPort);
        sb.append(", protocolVersion=").append(protocolVersion);
        sb.append('}');
        return sb.toString();
    }
}