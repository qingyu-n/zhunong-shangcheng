package com.zhunong.mall.controller;

import com.zhunong.mall.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/config")
public class ConfigController {

    @GetMapping
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("siteName", "助农商城");
        config.put("siteDescription", "连接城市与乡村的优质农产品平台");
        config.put("siteLogo", "/logo.png");
        config.put("contactPhone", "400-888-8888");
        config.put("contactEmail", "service@zhunong.com");
        config.put("version", "1.0.0");

        Map<String, Object> social = new HashMap<>();
        social.put("wechat", "zhunong_mall");
        social.put("weibo", "助农商城官方");
        social.put("douyin", "zhunong_mall");
        config.put("socialMedia", social);

        List<Map<String, Object>> friendLinks = new ArrayList<>();
        Map<String, Object> link1 = new HashMap<>();
        link1.put("name", "中国农业网");
        link1.put("url", "http://www.agri.cn");
        friendLinks.add(link1);
        Map<String, Object> link2 = new HashMap<>();
        link2.put("name", "农产品电商");
        link2.put("url", "http://www.nongec.com");
        friendLinks.add(link2);
        config.put("friendLinks", friendLinks);

        return Result.success(config);
    }
}
