package com.supzx.opcdemo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jinterop.dcom.common.JIException;
import org.openscada.opc.lib.common.AlreadyConnectedException;
import org.openscada.opc.lib.da.AutoReconnectListener;
import org.openscada.opc.lib.da.AutoReconnectState;
import org.openscada.opc.lib.da.ServerConnectionStateListener;

import java.net.UnknownHostException;

/**
 * @author ZhaoXu
 * @date 2022/10/19 18:34
 */
@Slf4j
public class OpcConnectListener implements ServerConnectionStateListener {
    @Override
    public void connectionStateChanged(boolean b) {
        if (!b) {
            log.warn("监测到opc连接断开，尝试重连！！！");
            try {
                OpcTestServiceImpl.server.connect();
            } catch (Exception e) {
                log.error("opc重连失败");
            }
            log.info("opc重连成功");
        }
    }
}
