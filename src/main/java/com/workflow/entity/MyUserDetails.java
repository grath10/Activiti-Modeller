package com.workflow.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class MyUserDetails extends SysUser implements UserDetails {
    private List<UserRole> roleList;

    public MyUserDetails(SysUser user, List<UserRole> roles) {
        super(user);
        this.roleList = roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roleList == null || roleList.size() < 1) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList("");
        }
        StringBuilder builder = new StringBuilder();
        for (UserRole role : roleList) {
            builder.append(role.getRole()).append(",");
        }
        String authorities = builder.substring(0, builder.length() - 1);
        return AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
    }

    @Override
    public boolean isAccountNonExpired() {
        return getAccountNonExpired() == 1;
    }

    @Override
    public boolean isAccountNonLocked() {
        return getAccountNonLocked() == 1;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return getCredentialsNonExpired() == 1;
    }

    @Override
    public boolean isEnabled() {
        return getEnabled() == 1;
    }
}
