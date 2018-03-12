package com.workflow.provider;

import com.workflow.service.UserService;
import com.workflow.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    public static final Logger logger = LoggerFactory.getLogger(LoginSuccessHandler.class);
    @Autowired
    private UserService userService;
    private String defaultTargetUrl = "/home";

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        saveLoginInfo(request, authentication);
        logger.info("登录成功，跳转至：" + this.defaultTargetUrl);
        request.getRequestDispatcher(this.defaultTargetUrl).forward(request, response);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void saveLoginInfo(HttpServletRequest request, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        WebAuthenticationDetails authenticationDetails = (WebAuthenticationDetails) authentication.getDetails();
        String userName = userDetails.getUsername();
        String password = userDetails.getPassword();
        String ip = getIpAddress(request);
        String address = authenticationDetails.getRemoteAddress();
        String time = DateUtil.getDateTimeFormat(new Date());
        logger.debug("详细信息:(IP：" + ip + ",地址：" + address + ",密码：" + password + ")");
        userService.updateUserInfo(time, userName);
    }

    public String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }


}
