package com.workflow.service;

import com.workflow.entity.LoginAttempts;
import com.workflow.entity.SysUser;
import com.workflow.mapper.LoginAttemptsMapper;
import com.workflow.mapper.SysUserMapper;
import com.workflow.utils.CommonUtils;
import com.workflow.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class LoginAttemptsService {
    private static final int MAX_ATTEMPTS = 3;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private LoginAttemptsMapper loginAttemptsMapper;

    public void updateFailAttempts(String username) {
        List<SysUser> user = userMapper.select(username);
        if (!CommonUtils.isEmpty(user)) {
            String time = DateUtil.getDateTimeFormat(new Date());
            LoginAttempts loginAttempts = loginAttemptsMapper.getLoginAttempts(username);
            if (loginAttempts == null) {
                LoginAttempts newAttempts = new LoginAttempts();
                newAttempts.setAttempts(1);
                newAttempts.setLastTime(time);
                newAttempts.setUsername(username);
                loginAttemptsMapper.insertLoginAttempts(newAttempts);
            } else {
                int current = loginAttempts.getAttempts();
                if (current + 1 >= MAX_ATTEMPTS) {
                    userMapper.lockUserByName(username);
                    throw new LockedException("用户账号已锁定，请联系管理员解锁");
                } else {
                    loginAttemptsMapper.updateFailAttempts(time, username);
                }
            }
        }
    }

    public LoginAttempts getLoginAttempts(String username) {
        return loginAttemptsMapper.getLoginAttempts(username);
    }

    public int updateLoginLastTime(String time, String username) {
        return userMapper.updateLoginTime(time, username);
    }

}
