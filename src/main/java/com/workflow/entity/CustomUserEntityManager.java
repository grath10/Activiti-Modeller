package com.workflow.entity;

import com.workflow.mapper.SysUserMapper;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.data.UserDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

// 自定义用户管理器
@Component
public class CustomUserEntityManager implements UserDataManager {
    @Autowired
    private SysUserMapper userMapper;

    public CustomUserEntityManager(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<User> findUserByQueryCriteria(UserQueryImpl query, Page page) {
        return null;
    }

    @Override
    public long findUserCountByQueryCriteria(UserQueryImpl query) {
        return 0;
    }

    @Override
    public List<Group> findGroupsByUser(String userId) {
        System.out.println("findGroupsByUser===");
        return null;
    }

    @Override
    public List<User> findUsersByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
        return null;
    }

    @Override
    public long findUserCountByNativeQuery(Map<String, Object> parameterMap) {
        return 0;
    }

    @Override
    public UserEntity create() {
        return null;
    }

    @Override
    public UserEntity findById(String entityId) {
        System.out.println("用户编号：" + entityId);
        System.out.println("findById=====");
        return null;
    }

    @Override
    public void insert(UserEntity entity) {

    }

    @Override
    public UserEntity update(UserEntity entity) {
        return null;
    }

    @Override
    public void delete(String id) {

    }

    @Override
    public void delete(UserEntity entity) {

    }
}
