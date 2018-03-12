package com.workflow.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.workflow.entity.activiti.*;
import com.workflow.entity.activiti.exception.BadRequestException;
import com.workflow.entity.activiti.exception.BaseModelerRestException;
import com.workflow.entity.activiti.exception.InternalServerErrorException;
import com.workflow.mapper.ModelMapper;
import com.workflow.service.ModelService;
import com.workflow.utils.CollectionUtils;
import com.workflow.utils.SecurityUtils;
import com.workflow.utils.XmlUtils;
import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.net.URLEncoder.encode;

@RestController
@RequestMapping("/editor/app/rest")
public class AppsController {
    protected static final int MIN_FILTER_LENGTH = 1;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected BpmnXMLConverter bpmnXmlConverter = new BpmnXMLConverter();
    protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ModelService modelService;
    @Autowired
    private ModelMapper modelMapper;

    @RequestMapping(value = "/models", method = RequestMethod.GET)
    public ResultListDataRepresentation getMyProcesses(HttpServletRequest request) {
        String filter = request.getParameter("filter");
        String type = request.getParameter("modelType");
        int modelType = Integer.parseInt(type);
        String sort = request.getParameter("sort");
        // need to parse the filterText parameter ourselves, due to encoding issues with the default parsing.
        String filterText = request.getParameter("filterText");
        List<ModelRepresentation> resultList = new ArrayList<>();
        List<Model> models = null;
        String validFilter = makeValidFilterText(filterText);
        String userId = SecurityUtils.getCurrentUser();
        if (validFilter != null) {
            models = modelMapper.findModelsCreatedByFilter(userId, modelType, validFilter);
        } else {
            models = modelMapper.findModelsCreatedBy(userId, modelType);
        }
        if (CollectionUtils.isNotEmpty(models)) {
            List<String> addedModelIds = new ArrayList<>();
            for (Model model : models) {
                if (addedModelIds.contains(model.getId()) == false) {
                    addedModelIds.add(model.getId());
                    ModelRepresentation representation = createModelRepresentation(model);
                    resultList.add(representation);
                }
            }
        }
        ResultListDataRepresentation result = new ResultListDataRepresentation(resultList);
        return result;
    }

    protected ModelRepresentation createModelRepresentation(AbstractModel model) {
        return new ModelRepresentation(model);
    }

    protected String makeValidFilterText(String filterText) {
        String validFilter = null;
        if (filterText != null) {
            String trimmed = StringUtils.trim(filterText);
            if (trimmed.length() >= MIN_FILTER_LENGTH) {
                validFilter = "%" + trimmed.toLowerCase() + "%";
            }
        }
        return validFilter;
    }

    // 创建自定义流程实例
    @RequestMapping(value = "/models", method = RequestMethod.POST)
    public ModelRepresentation createModel(@RequestBody ModelRepresentation modelRepresentation) {
        modelRepresentation.setKey(modelRepresentation.getKey().replaceAll(" ", ""));
        ModelKeyRepresentation modelKeyInfo = modelService.validateModelKey(null, modelRepresentation.getModelType(), modelRepresentation.getKey());
        if (modelKeyInfo.isKeyAlreadyExists()) {
            throw new BadRequestException("Provided model key already exists: " + modelRepresentation.getKey());
        }
        String json = null;
        if (modelRepresentation.getModelType() != null && modelRepresentation.getModelType().equals(AbstractModel.MODEL_TYPE_BPMN)) {
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.set("stencilset", stencilSetNode);
            ObjectNode propertiesNode = objectMapper.createObjectNode();
            propertiesNode.put("process_id", modelRepresentation.getKey());
            propertiesNode.put("name", modelRepresentation.getName());
            if (StringUtils.isNotEmpty(modelRepresentation.getDescription())) {
                propertiesNode.put("documentation", modelRepresentation.getDescription());
            }
            editorNode.set("properties", propertiesNode);
            ArrayNode childShapeArray = objectMapper.createArrayNode();
            editorNode.set("childShapes", childShapeArray);
            ObjectNode childNode = objectMapper.createObjectNode();
            childShapeArray.add(childNode);
            ObjectNode boundsNode = objectMapper.createObjectNode();
            childNode.set("bounds", boundsNode);
            ObjectNode lowerRightNode = objectMapper.createObjectNode();
            boundsNode.set("lowerRight", lowerRightNode);
            lowerRightNode.put("x", 130);
            lowerRightNode.put("y", 193);
            ObjectNode upperLeftNode = objectMapper.createObjectNode();
            boundsNode.set("upperLeft", upperLeftNode);
            upperLeftNode.put("x", 100);
            upperLeftNode.put("y", 163);
            childNode.set("childShapes", objectMapper.createArrayNode());
            childNode.set("dockers", objectMapper.createArrayNode());
            childNode.set("outgoing", objectMapper.createArrayNode());
            childNode.put("resourceId", "startEvent1");
            ObjectNode stencilNode = objectMapper.createObjectNode();
            childNode.set("stencil", stencilNode);
            stencilNode.put("id", "StartNoneEvent");
            json = editorNode.toString();
        }
        Model newModel = modelService.createModel(modelRepresentation, json, SecurityUtils.getCurrentUser());
        return new ModelRepresentation(newModel);
    }

    @RequestMapping(value = "/import-process-model", method = RequestMethod.POST, produces = "application/json")
    public ModelRepresentation importProcessModel(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        return importInternallyProcessModel(request, file);
    }

    /*
     * specific endpoint for IE9 flash upload component
     */
    @RequestMapping(value = "/import-process-model/text", method = RequestMethod.POST)
    public String importProcessModelText(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        ModelRepresentation modelRepresentation = importInternallyProcessModel(request, file);
        String modelRepresentationJson = null;
        try {
            modelRepresentationJson = objectMapper.writeValueAsString(modelRepresentation);
        } catch (Exception e) {
            logger.error("Error while processing Model representation json", e);
            throw new InternalServerErrorException("Model Representation could not be saved");
        }
        return modelRepresentationJson;
    }

    public ModelRepresentation importInternallyProcessModel(HttpServletRequest request, MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName != null && (fileName.endsWith(".bpmn") || fileName.endsWith(".bpmn20.xml"))) {
            try {
                XMLInputFactory xif = XmlUtils.createSafeXmlInputFactory();
                InputStreamReader xmlIn = new InputStreamReader(file.getInputStream(), "UTF-8");
                XMLStreamReader xtr = xif.createXMLStreamReader(xmlIn);
                BpmnModel bpmnModel = bpmnXmlConverter.convertToBpmnModel(xtr);
                if (CollectionUtils.isEmpty(bpmnModel.getProcesses())) {
                    throw new BadRequestException("No process found in definition " + fileName);
                }
                if (bpmnModel.getLocationMap().size() == 0) {
                    BpmnAutoLayout bpmnLayout = new BpmnAutoLayout(bpmnModel);
                    bpmnLayout.execute();
                }
                ObjectNode modelNode = bpmnJsonConverter.convertToJson(bpmnModel);
                Process process = bpmnModel.getMainProcess();
                String name = process.getId();
                if (StringUtils.isNotEmpty(process.getName())) {
                    name = process.getName();
                }
                String description = process.getDocumentation();
                ModelRepresentation model = new ModelRepresentation();
                model.setKey(process.getId());
                model.setName(name);
                model.setDescription(description);
                model.setModelType(AbstractModel.MODEL_TYPE_BPMN);
                Model newModel = modelService.createModel(model, modelNode.toString(), SecurityUtils.getCurrentUser());
                return new ModelRepresentation(newModel);
            } catch (BadRequestException e) {
                throw e;
            } catch (Exception e) {
                logger.error("Import failed for " + fileName, e);
                throw new BadRequestException("Import failed for " + fileName + ", error message " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Invalid file name, only .bpmn and .bpmn20.xml files are supported not " + fileName);
        }
    }

    /**
     * GET /rest/models/{modelId}/bpmn -> Get BPMN 2.0 xml
     */
    @RequestMapping(value = "/models/{processModelId}/bpmn20", method = RequestMethod.GET)
    public void getProcessModelBpmn20Xml(HttpServletResponse response, @PathVariable String processModelId) throws IOException {
        if (processModelId == null) {
            throw new BadRequestException("No process model id provided");
        }
        Model model = modelService.getModel(processModelId);
        generateBpmn20Xml(response, model);
    }

    /**
     * GET /rest/models/{modelId}/history/{processModelHistoryId}/bpmn20 -> Get BPMN 2.0 xml
     */
    @RequestMapping(value = "/models/{processModelId}/history/{processModelHistoryId}/bpmn20", method = RequestMethod.GET)
    public void getHistoricProcessModelBpmn20Xml(HttpServletResponse response, @PathVariable String processModelId, @PathVariable String processModelHistoryId) throws IOException {
        if (processModelId == null) {
            throw new BadRequestException("No process model id provided");
        }
        ModelHistory historicModel = modelService.getModelHistory(processModelId, processModelHistoryId);
        generateBpmn20Xml(response, historicModel);
    }

    protected void generateBpmn20Xml(HttpServletResponse response, AbstractModel model) {
        String name = model.getName().replaceAll(" ", "_");
        String fileName = null;
        try {
            fileName = encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".bpmn20.xml");
        if (model.getModelEditorJson() != null) {
            try {
                ServletOutputStream servletOutputStream = response.getOutputStream();
                response.setContentType("application/xml");
                BpmnModel bpmnModel = modelService.getBpmnModel(model);
                byte[] xmlBytes = modelService.getBpmnXML(bpmnModel);
                BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(xmlBytes));
                byte[] buffer = new byte[8096];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    servletOutputStream.write(buffer, 0, count);
                }
                // Flush and close stream
                servletOutputStream.flush();
                servletOutputStream.close();
            } catch (BaseModelerRestException e) {
                throw e;
            } catch (Exception e) {
                logger.error("Could not generate BPMN 2.0 XML", e);
                throw new InternalServerErrorException("Could not generate BPMN 2.0 xml");
            }
        }
    }

    @RequestMapping(value = "/rest/models/{modelId}/clone", method = RequestMethod.POST, produces = "application/json")
    public ModelRepresentation duplicateModel(@PathVariable String modelId, @RequestBody ModelRepresentation modelRepresentation) {

        String json = null;
        Model model = null;
        if (modelId != null) {
            model = modelService.getModel(modelId);
            json = model.getModelEditorJson();
        }

        if (model == null) {
            throw new InternalServerErrorException("Error duplicating model : Unknown original model");
        }

        if (modelRepresentation.getModelType() != null && modelRepresentation.getModelType().equals(AbstractModel.MODEL_TYPE_FORM)) {
            // nothing to do special for forms (just clone the json)
        } else if (modelRepresentation.getModelType() != null && modelRepresentation.getModelType().equals(AbstractModel.MODEL_TYPE_APP)) {
            // nothing to do special for applications (just clone the json)
        } else if (modelRepresentation.getModelType() != null && modelRepresentation.getModelType().equals(AbstractModel.MODEL_TYPE_DECISION_TABLE)) {
            // Decision Table model
            ObjectNode editorNode = null;

            try {

                editorNode = (ObjectNode) objectMapper.readTree(json);

                json = objectMapper.writeValueAsString(editorNode);


            } catch (Exception e) {
                logger.error("Error creating decision table model", e);
                throw new InternalServerErrorException("Error creating decision table");
            }


        } else {
            // BPMN model
            ObjectNode editorNode = null;
            try {
                ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(json);

                editorNode = deleteEmbededReferencesFromBPMNModel(editorJsonNode);

                ObjectNode propertiesNode = (ObjectNode) editorNode.get("properties");
                String processId = modelRepresentation.getName().replaceAll(" ", "");
                propertiesNode.put("process_id", processId);
                propertiesNode.put("name", modelRepresentation.getName());
                if (StringUtils.isNotEmpty(modelRepresentation.getDescription())) {
                    propertiesNode.put("documentation", modelRepresentation.getDescription());
                }
                editorNode.set("properties", propertiesNode);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (editorNode != null) {
                json = editorNode.toString();
            }
        }

        // create the new model
        Model newModel = modelService.createModel(modelRepresentation, json, SecurityUtils.getCurrentUser());

        // copy also the thumbnail
        byte[] imageBytes = model.getThumbnail();
        newModel = modelService.saveModel(newModel, newModel.getModelEditorJson(), imageBytes, false, newModel.getComment(), SecurityUtils.getCurrentUser());

        return new ModelRepresentation(newModel);
    }

    protected ObjectNode deleteEmbededReferencesFromBPMNModel(ObjectNode editorJsonNode) {
        try {
            internalDeleteNodeByNameFromBPMNModel(editorJsonNode, "formreference");
            internalDeleteNodeByNameFromBPMNModel(editorJsonNode, "subprocessreference");
            return editorJsonNode;
        } catch (Exception e) {
            throw new InternalServerErrorException("Cannot delete the external references");
        }
    }

    protected ObjectNode deleteEmbededReferencesFromStepModel(ObjectNode editorJsonNode) {
        try {
            JsonNode startFormNode = editorJsonNode.get("startForm");
            if (startFormNode != null) {
                editorJsonNode.remove("startForm");
            }
            internalDeleteNodeByNameFromStepModel(editorJsonNode.get("steps"), "formDefinition");
            internalDeleteNodeByNameFromStepModel(editorJsonNode.get("steps"), "subProcessDefinition");
            return editorJsonNode;
        } catch (Exception e) {
            throw new InternalServerErrorException("Cannot delete the external references");
        }
    }

    protected void internalDeleteNodeByNameFromBPMNModel(JsonNode editorJsonNode, String propertyName) {
        JsonNode childShapesNode = editorJsonNode.get("childShapes");
        if (childShapesNode != null && childShapesNode.isArray()) {
            ArrayNode childShapesArrayNode = (ArrayNode) childShapesNode;
            for (JsonNode childShapeNode : childShapesArrayNode) {
                // Properties
                ObjectNode properties = (ObjectNode) childShapeNode.get("properties");
                if (properties != null && properties.has(propertyName)) {
                    JsonNode propertyNode = properties.get(propertyName);
                    if (propertyNode != null) {
                        properties.remove(propertyName);
                    }
                }

                // Potential nested child shapes
                if (childShapeNode.has("childShapes")) {
                    internalDeleteNodeByNameFromBPMNModel(childShapeNode, propertyName);
                }

            }
        }
    }

    private void internalDeleteNodeByNameFromStepModel(JsonNode stepsNode, String propertyName) {

        if (stepsNode == null || !stepsNode.isArray()) {
            return;
        }

        for (JsonNode jsonNode : stepsNode) {

            ObjectNode stepNode = (ObjectNode) jsonNode;
            if (stepNode.has(propertyName)) {
                JsonNode propertyNode = stepNode.get(propertyName);
                if (propertyNode != null) {
                    stepNode.remove(propertyName);
                }
            }

            // Nested steps
            if (stepNode.has("steps")) {
                internalDeleteNodeByNameFromStepModel(stepNode.get("steps"), propertyName);
            }

            // Overdue steps
            if (stepNode.has("overdueSteps")) {
                internalDeleteNodeByNameFromStepModel(stepNode.get("overdueSteps"), propertyName);
            }

            // Choices is special, can have nested steps inside
            if (stepNode.has("choices")) {
                ArrayNode choicesArrayNode = (ArrayNode) stepNode.get("choices");
                for (JsonNode choiceNode : choicesArrayNode) {
                    if (choiceNode.has("steps")) {
                        internalDeleteNodeByNameFromStepModel(choiceNode.get("steps"), propertyName);
                    }
                }
            }
        }
    }

    @RequestMapping(value = "/**/rest/models/{modelId}/parent-relations", method = RequestMethod.GET, produces = "application/json")
    public List<ModelInformation> getModelRelations(@PathVariable String modelId) {
        Model model = modelService.getModel(modelId);
        if (model == null) {
            throw new NotFoundException();
        }
        return new ArrayList<>();
    }
}
