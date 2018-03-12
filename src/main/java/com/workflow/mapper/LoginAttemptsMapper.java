package com.workflow.mapper;

import com.workflow.entity.LoginAttempts;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface LoginAttemptsMapper {
    @Update("update login_attempts set attempts=attempts + 1, lastTime=#{lastTime} where username=#{username}")
    int updateFailAttempts(@Param("lastTime") String lastTime, @Param("username") String username);

    @Update("update login_attempts set attempts=0, lastTime=null where username=#{username}")
    int resetFailAttempts(String username);

    @Select("select * from login_attempts where username=#{username}")
    LoginAttempts getLoginAttempts(String username);

    @Insert("insert into login_attempts (username,attempts,lastTime) values (#{username}, #{attempts}, #{lastTime})")
    int insertLoginAttempts(LoginAttempts attempts);
}
