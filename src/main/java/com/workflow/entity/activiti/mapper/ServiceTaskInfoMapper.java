package com.workflow.entity.activiti.mapper;

import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;

public class ServiceTaskInfoMapper extends AbstractInfoMapper {
    protected void mapProperties(Object element) {
        ServiceTask serviceTask = (ServiceTask) element;
        if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(serviceTask.getImplementationType())) {
            createPropertyNode("Class", serviceTask.getImplementation());
        } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(serviceTask.getImplementationType())) {
            createPropertyNode("Expression", serviceTask.getImplementation());
        } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(serviceTask.getImplementationType())) {
            createPropertyNode("Delegate expression", serviceTask.getImplementation());
        }
        if (serviceTask.isAsynchronous()) {
            createPropertyNode("Asynchronous", true);
            createPropertyNode("Exclusive", !serviceTask.isNotExclusive());
        }
        if (ServiceTask.MAIL_TASK.equalsIgnoreCase(serviceTask.getType())) {
            createPropertyNode("Type", "Mail task");
        }
        createPropertyNode("Result variable name", serviceTask.getResultVariableName());
        createFieldPropertyNodes("Field extensions", serviceTask.getFieldExtensions());
        createListenerPropertyNodes("Execution listeners", serviceTask.getExecutionListeners());
    }
}
