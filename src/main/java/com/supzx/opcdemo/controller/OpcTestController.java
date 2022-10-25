package com.supzx.opcdemo.controller;

import com.supzx.opcdemo.service.OpcTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author ZhaoXu
 * @date 2022/10/19 14:40
 */
@RestController
@RequestMapping("/opc")
public class OpcTestController {
    @Autowired
    private OpcTestService opcTestService;

    @GetMapping("/readTag/{tags}")
    public Map<String, Object> readTag(@PathVariable String tags) {
        return opcTestService.readTag(tags);
    }

    @PutMapping("/write")
    public List<String> writeTag(@RequestBody Map<String, Object> tags) {
        return opcTestService.writeTag(tags);
    }
}
