package org.wanji.web.model.enums;


import org.wanji.netmc.session.Session;
import org.wanji.web.model.entity.PlatformDO;

public enum SessionKey {

    Device;

    /* 获取session对应的设备 */
    public static PlatformDO getPlatform(Session session) {
        return (PlatformDO) session.getAttribute(Device);
    }
}