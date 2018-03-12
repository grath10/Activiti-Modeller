package com.workflow.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.workflow.utils.CommonUtils;
import com.workflow.utils.XmlUtils;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

// 流程模型Model相关操作
@RestController
@RequestMapping("models")
public class ModelController {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ObjectMapper objectMapper;

    // 新建模型
    @PostMapping
    public Map<String, Object> newModel(@RequestBody Map<String, String> map) {
        Model model = repositoryService.newModel();
        String name = map.get("name");
        String key = map.get("key");
        String description = map.get("description");
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put("name", name);
        modelNode.put("description", description);
        modelNode.put("revision", 1);
        model.setName(name);
        model.setVersion(1);
        model.setKey(key);
        repositoryService.saveModel(model);
        String id = model.getId();
        // 完善ModelEditorSource
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.set("stencilset", stencilSetNode);
        try {
            repositoryService.addModelEditorSource(id, editorNode.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return CommonUtils.success();
    }

    // 获取所有模型
    @GetMapping
    public List<Model> getModelList() {
        return repositoryService.createModelQuery().orderByCreateTime().desc().list();
    }

    // 删除模型
    @DeleteMapping("{id}")
    public Object deleteModel(@PathVariable("id") String id) {
        repositoryService.deleteModel(id);
        return CommonUtils.success();
    }

    // 发布模型为流程定义
    @PostMapping("{id}/deployment")
    public Object deploy(@PathVariable("id") String id) {
        Model model = repositoryService.getModel(id);
        byte[] bytes = repositoryService.getModelEditorSource(model.getId());
        if (bytes == null) {
            return CommonUtils.failed("模型数据为空，先设计流程并保存成功再进行部署");
        }
        try {
            JsonNode modelNode = new ObjectMapper().readTree(bytes);
            BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            if (bpmnModel.getProcesses().size() == 0) {
                return CommonUtils.failed("数据模型不符合要求，请至少设计一条主线流程");
            }
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
            String name = model.getName() + ".bpmn20.xml";
            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
                    .name(model.getName()).addString(name, new String(bpmnBytes, "utf-8"));
            Deployment deployment = deploymentBuilder.deploy();
            model.setDeploymentId(deployment.getId());
            repositoryService.saveModel(model);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CommonUtils.success();
    }

    // 上传已有模型
    @PostMapping("/uploadFile")
    public void deployUploadedFile(@RequestParam("uploadFile") MultipartFile uploadFile) {
        InputStreamReader in = null;
        String fileName = uploadFile.getOriginalFilename();
        if (fileName.endsWith(".bpmn20.xml") || fileName.endsWith(".bpmn")) {
            XMLInputFactory xif = XmlUtils.createSafeXmlInputFactory();
            try {
                in = new InputStreamReader(new ByteArrayInputStream(uploadFile.getBytes()), "utf-8");
                XMLStreamReader xtr = xif.createXMLStreamReader(in);
                BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
                if (bpmnModel.getMainProcess() != null && bpmnModel.getMainProcess().getId() != null) {
                    if (!bpmnModel.getLabelLocationMap().isEmpty()) {
                        String processName = null;
                        if (StringUtils.isNotEmpty(bpmnModel.getMainProcess().getName())) {
                            processName = bpmnModel.getMainProcess().getName();
                        } else {
                            processName = bpmnModel.getMainProcess().getId();
                        }
                        Model model = repositoryService.newModel();
                        ObjectNode modelNode = new ObjectMapper().createObjectNode();
                        modelNode.put("name", processName);
                        modelNode.put("version", 1);
                        model.setName(processName);
                        repositoryService.saveModel(model);
                        BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
                        ObjectNode editorNode = jsonConverter.convertToJson(bpmnModel);
                        repositoryService.addModelEditorSource(model.getId(), editorNode.toString().getBytes("utf-8"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
