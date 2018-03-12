package com.workflow.entity.activiti.mapper;

import org.activiti.bpmn.model.SequenceFlow;
import org.apache.commons.lang3.StringUtils;

public class SequenceFlowInfoMapper extends AbstractInfoMapper {
    protected void mapProperties(Object element) {
        SequenceFlow sequenceFlow = (SequenceFlow) element;

        if (StringUtils.isNotEmpty(sequenceFlow.getConditionExpression())) {
            createPropertyNode("Condition expression", sequenceFlow.getConditionExpression());
        }

        createListenerPropertyNodes("Execution listeners", sequenceFlow.getExecutionListeners());
    }
}
