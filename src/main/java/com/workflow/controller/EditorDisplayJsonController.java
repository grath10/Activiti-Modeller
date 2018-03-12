package com.workflow.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.workflow.entity.activiti.BpmnDisplayJsonConverter;
import com.workflow.entity.activiti.Model;
import com.workflow.entity.activiti.ModelHistory;
import com.workflow.service.ModelService;
import org.activiti.bpmn.model.GraphicInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EditorDisplayJsonController {
    @Autowired
    protected ModelService modelService;

    @Autowired
    protected BpmnDisplayJsonConverter bpmnDisplayJsonConverter;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(value = "/**/rest/models/{processModelId}/model-json", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getModelJSON(@PathVariable String processModelId) {
        ObjectNode displayNode = objectMapper.createObjectNode();
        Model model = modelService.getModel(processModelId);
        bpmnDisplayJsonConverter.processProcessElements(model, displayNode, new GraphicInfo());
        return displayNode;
    }

    @RequestMapping(value = "/**/rest/models/{processModelId}/history/{processModelHistoryId}/model-json", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getModelHistoryJSON(@PathVariable String processModelId, @PathVariable String processModelHistoryId) {
        ObjectNode displayNode = objectMapper.createObjectNode();
        ModelHistory model = modelService.getModelHistory(processModelId, processModelHistoryId);
        bpmnDisplayJsonConverter.processProcessElements(model, displayNode, new GraphicInfo());
        return displayNode;
    }
}
