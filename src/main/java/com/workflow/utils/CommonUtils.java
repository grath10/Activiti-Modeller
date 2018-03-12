package com.workflow.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonUtils {
    public static boolean isEmpty(List<?> list) {
        return list == null || list.size() == 0;
    }

    public static Map<String, Object> success() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", true);
        map.put("reason", "操作成功");
        return map;
    }

    public static Map<String, Object> failed(String reason) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", false);
        map.put("reason", "操作失败：" + reason);
        return map;
    }
}
