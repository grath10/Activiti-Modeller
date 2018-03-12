package com.workflow.entity.activiti.mapper;

import com.workflow.utils.CollectionUtils;
import org.activiti.bpmn.model.*;
import org.apache.commons.lang3.StringUtils;

public class EventInfoMapper extends AbstractInfoMapper{
    protected void mapProperties(Object element) {
        Event event = (Event) element;
        if (CollectionUtils.isNotEmpty(event.getEventDefinitions())) {
            EventDefinition eventDef = event.getEventDefinitions().get(0);
            if (eventDef instanceof TimerEventDefinition) {
                TimerEventDefinition timerDef = (TimerEventDefinition) eventDef;
                if (StringUtils.isNotEmpty(timerDef.getTimeDate())) {
                    createPropertyNode("Timer date", timerDef.getTimeDate());
                }
                if (StringUtils.isNotEmpty(timerDef.getTimeDuration())) {
                    createPropertyNode("Timer duration", timerDef.getTimeDuration());
                }
                if (StringUtils.isNotEmpty(timerDef.getTimeDuration())) {
                    createPropertyNode("Timer cycle", timerDef.getTimeCycle());
                }

            } else if (eventDef instanceof SignalEventDefinition) {
                SignalEventDefinition signalDef = (SignalEventDefinition) eventDef;
                if (StringUtils.isNotEmpty(signalDef.getSignalRef())) {
                    createPropertyNode("Signal ref", signalDef.getSignalRef());
                }

            } else if (eventDef instanceof MessageEventDefinition) {
                MessageEventDefinition messageDef = (MessageEventDefinition) eventDef;
                if (StringUtils.isNotEmpty(messageDef.getMessageRef())) {
                    createPropertyNode("Message ref", messageDef.getMessageRef());
                }

            } else if (eventDef instanceof ErrorEventDefinition) {
                ErrorEventDefinition errorDef = (ErrorEventDefinition) eventDef;
                if (StringUtils.isNotEmpty(errorDef.getErrorCode())) {
                    createPropertyNode("Error code", errorDef.getErrorCode());
                }
            }
        }
        createListenerPropertyNodes("Execution listeners", event.getExecutionListeners());
    }
}