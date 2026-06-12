package com.zhunong.mall.controller;

import com.zhunong.mall.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/activity")
public class ActivityController {

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getActivityList() {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        Map<String, Object> activity1 = new HashMap<>();
        activity1.put("id", 1);
        activity1.put("title", "春季助农大促");
        activity1.put("description", "精选农产品限时特惠");
        activity1.put("image", "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=800");
        activity1.put("status", 1);
        activities.add(activity1);
        
        Map<String, Object> activity2 = new HashMap<>();
        activity2.put("id", 2);
        activity2.put("title", "新品尝鲜季");
        activity2.put("description", "新农品上市优惠");
        activity2.put("image", "https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=800");
        activity2.put("status", 1);
        activities.add(activity2);

        return Result.success(activities);
    }
}
