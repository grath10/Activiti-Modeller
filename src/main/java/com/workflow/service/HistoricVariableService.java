package com.workflow.service;

import com.workflow.entity.HistoricVariable;
import com.workflow.mapper.HistoricVariableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HistoricVariableService {
    @Autowired
    private HistoricVariableMapper historicVariableMapper;

    public int saveHistoricVariable(HistoricVariable variableInstanceEntity) {
        return historicVariableMapper.insertVariable(variableInstanceEntity);
    }

    public HistoricVariable getVariable(String processInstanceId, String taskId) {
        Map<String, String> params = new HashMap<>();
        params.put("processInstanceId", processInstanceId);
        params.put("taskId", taskId);
        return historicVariableMapper.getVariable(params);
    }

    public int deleteVariable(String processInstanceId) {
        return historicVariableMapper.deleteVariables(processInstanceId);
    }
}
