package com.workflow.service;

import com.workflow.entity.Menu;
import com.workflow.mapper.MenuMapper;
import com.workflow.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MenuService {
    @Autowired
    private MenuMapper menuMapper;

    public List<Menu> getDisplayedMenus(String role) {
        List<Menu> menuList = menuMapper.getMenusForRole(role);
        Map<String, List<Menu>> menuMap = extractMenuOnLevel(menuList);
        return buildMenus(menuMap, "-1");
    }

    public List<Integer> getAvailableMenu(String roleId) {
        return menuMapper.getMenuIdsForRole(roleId);
    }

    public List<Menu> getMenuWholeSet() {
        List<Menu> menuList = menuMapper.getAllMenus();
        Map<String, List<Menu>> menuMap = extractMenuOnLevel(menuList);
        return buildMenus(menuMap, "-1");
    }

    private Map<String, List<Menu>> extractMenuOnLevel(List<Menu> menuList) {
        Map<String, List<Menu>> menuMap = new HashMap<>();
        if (!CommonUtils.isEmpty(menuList)) {
            for (Menu menu : menuList) {
                int parent = menu.getParent();
                List<Menu> menus = menuMap.get(parent + "");
                if (CommonUtils.isEmpty(menus)) {
                    menus = new ArrayList<>();
                }
                menus.add(menu);
                menuMap.put(parent + "", menus);
            }
        }
        return menuMap;
    }

    private List<Menu> buildMenus(Map<String, List<Menu>> menuMap, String id) {
        List<Menu> menuList = menuMap.get(id);
        if (menuList == null || menuList.size() == 0) {
            return null;
        }
        for (Menu menu : menuList) {
            int index = menu.getId();
            List<Menu> children = buildMenus(menuMap, index + "");
            menu.setChildren(children);
            if (CommonUtils.isEmpty(children)) {
                menu.setLeaf(true);
            }
        }
        return menuList;
    }

    public int savePrivilege(String roleId, List<String> menuList) {
        menuMapper.deletePermission(roleId);
        return menuMapper.insertPermission(menuList, roleId);
    }
}
