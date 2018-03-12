package com.workflow.entity;

import com.workflow.mapper.UserRoleMapper;
import com.workflow.utils.CommonUtils;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.GroupEntityImpl;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisGroupDataManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomGroupEntityManager extends MybatisGroupDataManager {
    private UserRoleMapper userRoleMapper;

    public CustomGroupEntityManager(ProcessEngineConfigurationImpl processEngineConfiguration, UserRoleMapper roleMapper) {
        super(processEngineConfiguration);
        this.userRoleMapper = roleMapper;
    }

    @Override
    public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
        return getDbSqlSession().selectList("selectGroupByQueryCriteria", query, page);
    }

    @Override
    public List<Group> findGroupsByUser(String userId) {
        List<UserRole> roleList = userRoleMapper.getRolesByUserId(userId);
        List<Group> groups = new ArrayList<>();
        if (!CommonUtils.isEmpty(roleList)) {
            for (UserRole role : roleList) {
                String desc = role.getDescription();
                String name = role.getRole();
                GroupEntityImpl groupEntity = new GroupEntityImpl();
                groupEntity.setName(name);
                groupEntity.setType(desc);
                groupEntity.setId(name);
                groups.add(groupEntity);
            }
        }
        return groups;
    }
}
