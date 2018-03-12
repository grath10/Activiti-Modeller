package com.workflow.controller;

import com.github.pagehelper.PageHelper;
import com.workflow.entity.DataTableBean;
import com.workflow.entity.UserRole;
import com.workflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/role")
public class RoleController {
    @Autowired
    private UserService userService;

    @RequestMapping("")
    public String role(Model model) {
        model.addAttribute("type", "role");
        return "role";
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
        List<UserRole> userList = userService.getRoleForTable(id, column, order);
        DataTableBean dtBean = new DataTableBean();
        dtBean.setDraw(draw);
        dtBean.setList(userList);
        dtBean.setTotal(userList.size());
        return dtBean;
    }

    @RequestMapping("/findOne")
    @ResponseBody
    public UserRole querySingleRole(@RequestParam("id") String role) {
        return userService.getRoleInfo(role);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public int deleteRole(@RequestParam("id") String role) {
        return userService.deleteRole(role);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public boolean saveUser(@RequestBody Map<String, String> requestMap) {
        boolean val = false;
        String operation = requestMap.get("operation");
        String remark = requestMap.get("remark");
        String role = requestMap.get("role");
        String desc = requestMap.get("desc");
        UserRole roleBean = new UserRole();
        roleBean.setRemark(remark);
        roleBean.setDescription(desc);
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }
        roleBean.setRole(role);
        if ("create".equals(operation)) {
            val = userService.insertRole(roleBean);
        } else if ("update".equals(operation)) {
            val = userService.updateRole(roleBean);
        }
        return val;
    }
}
