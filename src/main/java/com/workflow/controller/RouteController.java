package com.workflow.controller;

import com.workflow.entity.Menu;
import com.workflow.entity.SysUser;
import com.workflow.service.MenuService;
import com.workflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.List;

@Controller
public class RouteController {
    @Autowired
    private MenuService menuService;
    @Autowired
    private UserService userService;

    @RequestMapping(value = {"/", "home"})
    public String home(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities != null && authorities.size() > 0) {
            GrantedAuthority[] authArr = authorities.toArray(new GrantedAuthority[0]);
            GrantedAuthority authority = authArr[0];
            String permission = authority.getAuthority();
            List<Menu> menuList = menuService.getDisplayedMenus(permission);
            model.addAttribute("menu", menuList);
        }
        String username = authentication.getName();
        SysUser sysUser = userService.getUserInfo(username);
        model.addAttribute("loginUser", sysUser);
        return "home";
    }

    @GetMapping("login")
    public String login() {
        return "login";
    }

    @GetMapping("logout")
    public String logout() {
        return "redirect:/login";
    }

    @RequestMapping("/modeler")
    public String model() {
        return "modeler";
    }
}