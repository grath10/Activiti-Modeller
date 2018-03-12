package com.workflow.mapper;

import com.workflow.entity.SysUser;
import com.workflow.entity.UserRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserRoleMapper {
    @Select("select role, description from user_role where id=#{user.roleId}")
    List<UserRole> getRolesByUser(@Param("user") SysUser user);

    @Select("select id, description from user_role")
    List<UserRole> getRoleList();

    @Select("select a.role, a.description from user_role a, user b where a.id=b.roleId and b.username=#{userId}")
    List<UserRole> getRolesByUserId(String userId);

    @Select("select * from user_role where role like concat(concat('%',#{id}),'%') order by ${column} ${dir}")
    List<UserRole> findRoles(@Param("id") String id, @Param("column") String column, @Param("dir") String dir);

    @Select("select * from user_role where role like concat(concat('%',#{id}),'%')")
    List<UserRole> findRolesLikeName(@Param("id") String id);

    @Select("select role, description from user_role where role=#{role}")
    UserRole getRoleDetail(String role);

    @Delete("delete from user_role where role=#{role}")
    int deleteRole(String role);

    @Insert("insert into user_role (role, description, remark) values(#{role},#{description},#{remark})")
    int insertRole(UserRole role);

    @Update("update user_role set description=#{description} where role=#{role}")
    int updateRole(UserRole role);
}
