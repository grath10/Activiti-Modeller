package com.workflow.service;

import com.workflow.entity.SysUser;
import com.workflow.entity.UserRole;
import com.workflow.mapper.SysUserMapper;
import com.workflow.mapper.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private UserRoleMapper roleMapper;

    public void updateUserInfo(String time, String name) {
        userMapper.updateLoginTime(time, name);
    }

    public List<SysUser> getUserList(String id, String column, String order) {
        return userMapper.findUsers(id, column, order);
    }

    public boolean lockUser(int id) {
        return userMapper.lockUser(id) == 1;
    }

    public boolean unlockUser(int id) {
        return userMapper.unlockUser(id) == 1;
    }

    public SysUser getUserInfo(String username) {
        return userMapper.getUserDetail(username);
    }

    public boolean insertOneUser(SysUser user) {
        return userMapper.insertNewUser(user) == 1;
    }

    public boolean updateUser(SysUser user) {
        return userMapper.updateUserInfo(user) == 1;
    }

    public List<UserRole> getRoleList() {
        return roleMapper.getRoleList();
    }

    public List<UserRole> getRoleForTable(String id, String column, String order) {
        return roleMapper.findRoles(id, column, order);
    }

    public UserRole getRoleInfo(String role) {
        return roleMapper.getRoleDetail(role);
    }

    public int deleteRole(String role) {
        return roleMapper.deleteRole(role);
    }

    public boolean insertRole(UserRole role) {
        return roleMapper.insertRole(role) == 1;
    }

    public boolean updateRole(UserRole role) {
        return roleMapper.updateRole(role) == 1;
    }

    public List<UserRole> getMatchingRoles(String id){
        String name = id.toUpperCase();
        return roleMapper.findRolesLikeName(name);
    }

    public List<SysUser> findFuzzyUsers(String id){
        return userMapper.findFuzzyUsers(id);
    }

    public int countFuzzyUsers(String id){
        return userMapper.countFuzzyUsers(id);
    }
}
