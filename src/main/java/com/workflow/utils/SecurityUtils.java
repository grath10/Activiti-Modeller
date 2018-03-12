package com.workflow.utils;

import com.workflow.entity.activiti.ActivitiAppUser;
import com.workflow.entity.activiti.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private static User assumeUser;

    private SecurityUtils() {
    }

    /**
     * Get the login of the current user.
     */
    public static String getCurrentUserId() {
        User user = getCurrentUserObject();
        if (user != null) {
            return user.getId();
        }
        return null;
    }

    public static String getCurrentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * @return the {@link User} object associated with the current logged in user.
     */
    public static User getCurrentUserObject() {
        if (assumeUser != null) {
            return assumeUser;
        }

        User user = null;
        ActivitiAppUser appUser = getCurrentActivitiAppUser();
        if (appUser != null) {
            user = appUser.getUserObject();
        }
        return user;
    }

    public static ActivitiAppUser getCurrentActivitiAppUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        ActivitiAppUser user = null;
        if (securityContext != null && securityContext.getAuthentication() != null) {
            Object principal = securityContext.getAuthentication().getPrincipal();
            if (principal != null && principal instanceof ActivitiAppUser) {
                user = (ActivitiAppUser) principal;
            }
        }
        return user;
    }

    public static boolean currentUserHasCapability(String capability) {
        ActivitiAppUser user = getCurrentActivitiAppUser();
        for (GrantedAuthority grantedAuthority : user.getAuthorities()) {
            if (capability.equals(grantedAuthority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    public static void assumeUser(User user) {
        assumeUser = user;
    }

    public static void clearAssumeUser() {
        assumeUser = null;
    }
}
