package com.workflow.mapper;

import com.workflow.entity.HistoricVariable;
import org.apache.ibatis.annotations.Delete;

import java.util.Map;

public interface HistoricVariableMapper {
    int insertVariable(HistoricVariable variableInstanceEntity);

    HistoricVariable getVariable(Map<String, String> params);

    @Delete("delete from hi_variable where PROC_INST_ID_ = #{processInstanceId}")
    int deleteVariables(String processInstanceId);
}