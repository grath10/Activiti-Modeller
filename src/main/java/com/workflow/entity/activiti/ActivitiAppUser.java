package com.workflow.entity.activiti;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class ActivitiAppUser extends User {
    private static final long serialVersionUID = 1L;

    protected com.workflow.entity.activiti.User userObject;

    /**
     * The userId needs to be passed explicitly. It can be the email, but also the external id when eg LDAP is being used.
     */
    public ActivitiAppUser(com.workflow.entity.activiti.User user, String userId, Collection<? extends GrantedAuthority> authorities) {
        super(userId, user.getPassword() != null ? user.getPassword() : "", authorities); // Passwords needs to be non-null. Even if it's not there (eg LDAP auth)
        this.userObject = user;
    }

    public com.workflow.entity.activiti.User getUserObject() {
        return userObject;
    }
}
