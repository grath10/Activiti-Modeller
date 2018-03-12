package com.workflow.entity;

public class SysUser {
    private int id;
    private String username;
    private String password;
    private int enabled;
    private int accountNonLocked;
    private int accountNonExpired;
    private int credentialsNonExpired;
    private String lastLoginTime;
    private String role;
    private int roleId;

    public SysUser() {
    }

    public SysUser(SysUser user) {
        this.id = user.getId();
        this.password = user.getPassword();
        this.username = user.getUsername();
        this.enabled = user.getEnabled();
        this.accountNonExpired = user.getAccountNonExpired();
        this.accountNonLocked = user.getAccountNonLocked();
        this.credentialsNonExpired = user.getCredentialsNonExpired();
    }

    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(int accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public int getAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(int accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public int getCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(int credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }
}
