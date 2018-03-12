package com.workflow.controller;

import com.github.pagehelper.PageHelper;
import com.workflow.entity.DataTableBean;
import com.workflow.entity.SysUser;
import com.workflow.entity.UserRole;
import com.workflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RequestMapping("/user")
@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("")
    public String user(Model model) {
        model.addAttribute("type", "user");
        return "user";
    }

    @RequestMapping("/role")
    @ResponseBody
    public List<UserRole> getRoleList() {
        return userService.getRoleList();
    }

    @RequestMapping("/list")
    @ResponseBody
    public DataTableBean getList(HttpServletRequest request) {
        String draw = request.getParameter("draw");
        String order = request.getParameter("dir");
        String column = request.getParameter("column");
        String start = request.getParameter("start");
        String page = request.getParameter("page");
        String id = request.getParameter("id");
        int pageNum = Integer.parseInt(start);
        int pageSize = Integer.parseInt(page);
        PageHelper.startPage(pageNum, pageSize);
        List<SysUser> userList = userService.getUserList(id, column, order);
        DataTableBean dtBean = new DataTableBean();
        dtBean.setDraw(draw);
        dtBean.setList(userList);
        dtBean.setTotal(userList.size());
        return dtBean;
    }

    @RequestMapping("/findOne")
    @ResponseBody
    public SysUser querySingleUser(@RequestParam("id") String username) {
        return userService.getUserInfo(username);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public boolean saveUser(@RequestBody Map<String, String> requestMap) {
        boolean val = false;
        String operation = requestMap.get("operation");
        String username = requestMap.get("username");
        String password = requestMap.get("password");
        String roleId = requestMap.get("roleId");
        SysUser user = new SysUser();
        user.setPassword(password);
        user.setUsername(username);
        user.setRoleId(Integer.parseInt(roleId));
        if ("create".equals(operation)) {
            val = userService.insertOneUser(user);
        } else if ("update".equals(operation)) {
            val = userService.updateUser(user);
        }
        return val;
    }

    @RequestMapping("/lock")
    @ResponseBody
    public boolean lockUser(@RequestParam("id") String id) {
        int key = Integer.parseInt(id);
        return userService.lockUser(key);
    }

    @RequestMapping("/unlock")
    @ResponseBody
    public boolean unlockUser(@RequestParam("id") String id) {
        int key = Integer.parseInt(id);
        return userService.unlockUser(key);
    }
}
