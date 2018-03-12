package com.workflow.mapper;

import com.workflow.entity.Leave;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface LeaveMapper {
    @Insert("insert into leave_request (userId, processInstanceId, startTime, endTime, applyTime, reason) values(#{userId}, #{processInstanceId}, #{startTime}, #{endTime}, #{applyTime}, #{reason})")
    int saveLeave(Leave leave);

    @Select("select * from leave_request where processInstanceId=#{processInstanceId}")
    Leave getLeaveInfo(String processInstanceId);

    @Update("update leave_request set deptApproved=#{result} where processInstanceId=#{processDefinitionId}")
    int updateLeaderApprovement(@Param("processDefinitionId") String processDefinitionId, @Param("result") String result);

    @Update("update leave_request set hrApproved=#{result} where processInstanceId=#{processDefinitionId}")
    int updateHrApprovement(@Param("processDefinitionId") String processDefinitionId, @Param("result") String result);

}
