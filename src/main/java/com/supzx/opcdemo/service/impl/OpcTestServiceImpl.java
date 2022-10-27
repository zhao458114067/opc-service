package com.supzx.opcdemo.service.impl;

import com.supzx.opcdemo.service.OpcTestService;
import lombok.extern.slf4j.Slf4j;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIVariant;
import org.openscada.opc.dcom.da.OPCSERVERSTATUS;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.da.Group;
import org.openscada.opc.lib.da.Item;
import org.openscada.opc.lib.da.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author ZhaoXu
 * @date 2022/10/19 14:41
 */
@Service
@Slf4j
public class OpcTestServiceImpl implements OpcTestService, ApplicationRunner {
    @Value("${opc.userName}")
    String userName;

    @Value("${opc.password}")
    String password;

    @Value("${opc.host}")
    String host;

    @Value("${opc.domain}")
    String domain;

    @Value("${opc.clsId}")
    String clsId;

    @Value("${opc.progId}")
    String progId;

    @Resource(name = "opcExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public static Server server;

    static Map<String, Group> groupMap = new ConcurrentSkipListMap<>();

    @Override
    public Map<String, Object> readTag(String tags) {
        Map<String, Object> readResult = new HashMap<>(8);
        Map<String, Item> stringItemMap = getItemsMap(tags.split(","));
        try {
            stringItemMap.forEach((tag, item) -> {
                Object object = null;
                try {
                    object = item.read(true).getValue().getObject();
                } catch (JIException e) {
                    log.error("位号 {} 读值失败：{}", tag, object);
                }
                log.info("位号 {} 读值成功：{}", tag, object);
                readResult.put(tag, object);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return readResult;
    }

    @Override
    public List<String> writeTag(Map<String, Object> tags) {
        List<String> tagList = new ArrayList<>(tags.keySet());
        List<String> errTagList = new ArrayList<>();
        String[] tagsString = new String[tagList.size()];
        for (int i = 0; i < tagList.size(); i++) {
            String tag = tagList.get(i);
            tagsString[i] = tag;
        }
        Map<String, Item> stringItemMap = getItemsMap(tagsString);
        stringItemMap.forEach((tag, item) -> {
            String writeValue = tags.get(tag).toString();
            JIVariant jiVariant = new JIVariant(writeValue);
            try {
                item.write(jiVariant);
            } catch (JIException e) {
                errTagList.add(tag);
                log.error("位号 {} 写值 {} 失败", tag, writeValue);
            }
            log.info("位号 {} 写值成功：{}", tag, writeValue);
        });
        return errTagList;
    }

    private Map<String, Item> getItemsMap(String[] tags) {
        Group group = groupMap.get("Group one");
        log.info("添加位号：{}", (Object) tags);
        Map<String, Item> stringItemMap = null;
        try {
            stringItemMap = group.addItems(tags);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringItemMap;
    }

    @Override
    public void run(ApplicationArguments args) {
        connectOpc();
        connectHeartBeat();
    }

    private void connectHeartBeat() {
        threadPoolTaskExecutor.execute(() -> {
            while (true) {
                OPCSERVERSTATUS serverState = server.getServerState();

                if (serverState == null) {
                    log.warn("连接已断开，尝试重连！！！");
                    server.disconnect();
                    connectOpc();
                } else {
                    log.info("send连接心跳成功。");
                }
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void connectOpc() {
        log.info("开始创建opc连接");
        ConnectionInformation connectionInformation = new ConnectionInformation();
        connectionInformation.setHost(host);
        connectionInformation.setDomain(domain);
        connectionInformation.setUser(userName);
        connectionInformation.setPassword(password);
        connectionInformation.setClsid(clsId);
        connectionInformation.setProgId(progId);
        server = new Server(connectionInformation, new ScheduledThreadPoolExecutor(20, Executors.defaultThreadFactory()));

        log.info("开始建立opc连接");
        try {
            server.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("建立opc连接成功");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("添加分组Group one");
        Group group = null;
        try {
            group = server.addGroup("Group one");
        } catch (Exception e) {
            log.error("添加分组Group one失败！");
        }
        groupMap.put("Group one", group);
        try {
            group.setActive(true);
        } catch (JIException e) {
            log.error("分组激活失败！");
        }
//        OpcConnectListener opcConnectListener = new OpcConnectListener();
//        autoReconnectController.addListener(opcConnectListener);
    }
}
