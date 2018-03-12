package com.workflow.mapper;

import com.workflow.entity.SysUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface SysUserMapper {
    @Select("select * from user where username=#{name}")
    List<SysUser> select(@Param("name") String username);

    @Update("update user set enabled=0 where username=#{username}")
    int disableUser(String username);

    @Update("update user set accountNonLocked=0 where id=#{id}")
    int lockUser(int id);

    @Update("update user set accountNonLocked=0 where username=#{name}")
    int lockUserByName(String name);

    @Update("update user set accountNonLocked=1 where id=#{id}")
    int unlockUser(int id);

    @Update("update user set lastLoginTime=#{time} where username=#{username}")
    int updateLoginTime(@Param("time") String time, @Param("username") String username);

    @Select("select * from user where username like concat(concat('%',#{id}),'%') order by ${column} ${dir}")
    List<SysUser> findUsers(@Param("id") String id, @Param("column") String column, @Param("dir") String dir);

    @Select("select * from user where username like concat(concat('%',#{id}),'%')")
    List<SysUser> findFuzzyUsers(@Param("id") String id);

    @Select("select count(1) from user where username like concat(concat('%',#{id}),'%')")
    int countFuzzyUsers(@Param("id") String id);

    @Select("select username,password,roleId,lastLoginTime from user where username=#{username}")
    SysUser getUserDetail(String username);

    @Update("update user set password=#{password}, roleId=#{roleId} where username=#{username}")
    int updateUserInfo(SysUser user);

    @Insert("insert user (username,password,roleId) values(#{username},#{password},#{roleId})")
    int insertNewUser(SysUser user);
}
