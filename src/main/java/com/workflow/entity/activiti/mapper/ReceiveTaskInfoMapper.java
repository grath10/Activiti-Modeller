package com.workflow.entity.activiti.mapper;

import org.activiti.bpmn.model.ReceiveTask;

public class ReceiveTaskInfoMapper extends AbstractInfoMapper {
    protected void mapProperties(Object element) {
        ReceiveTask receiveTask = (ReceiveTask) element;
        createListenerPropertyNodes("Execution listeners", receiveTask.getExecutionListeners());
    }
}
