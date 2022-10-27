package com.supzx.opcdemo.service;

import java.util.List;
import java.util.Map;

/**
 * @author ZhaoXu
 * @date 2022/10/19 14:41
 */
public interface OpcTestService {

    /**
     * 读取位号值
     *
     * @param tags
     * @return
     */
    Map<String, Object> readTag(String tags);

    /**
     * 读取位号值
     *
     * @param tags
     * @return
     */
    List<String> writeTag(Map<String, Object> tags);

    /**
     * 建立连接
     */
    public void connectOpc();
}
