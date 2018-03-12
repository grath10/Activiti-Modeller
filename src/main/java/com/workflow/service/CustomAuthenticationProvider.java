package com.workflow.service;

import com.workflow.entity.MyUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private LoginAttemptsService loginAttemptsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = authentication.getName();
        String password = (String) authentication.getCredentials();
        MyUserDetails details = (MyUserDetails) userDetailsService.loadUserByUsername(userName);
        logger.info("输入密码/实际密码：" + password + "/" + details.getPassword());
        if (details == null) {
            throw new BadCredentialsException("用户名不存在");
        }
        // 密码匹配验证
        if (!password.equals(details.getPassword())) {
            loginAttemptsService.updateFailAttempts(userName);
            throw new BadCredentialsException("密码错误");
        }
        int locked = details.getAccountNonLocked();
        if (locked == 0) {
            throw new LockedException("用户账号已锁定，请联系管理员解锁");
        }
        Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
        return new UsernamePasswordAuthenticationToken(details, password, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
