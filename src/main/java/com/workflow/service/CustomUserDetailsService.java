package com.workflow.service;

import com.workflow.entity.MyUserDetails;
import com.workflow.entity.SysUser;
import com.workflow.entity.UserRole;
import com.workflow.mapper.SysUserMapper;
import com.workflow.mapper.UserRoleMapper;
import com.workflow.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    // 根据用户名从数据库中查找用户
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("loadUserByUsername", username);
        List<SysUser> userList = sysUserMapper.select(username);
        if (CommonUtils.isEmpty(userList)) {
            throw new UsernameNotFoundException("用户名未找到");
        } else {
            SysUser user = userList.get(0);
            List<UserRole> roles = userRoleMapper.getRolesByUser(user);
            return new MyUserDetails(user, roles);
        }
    }
}
