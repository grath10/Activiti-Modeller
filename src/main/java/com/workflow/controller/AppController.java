package com.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.workflow.entity.activiti.AbstractModel;
import com.workflow.entity.activiti.Model;
import com.workflow.entity.activiti.ModelKeyRepresentation;
import com.workflow.entity.activiti.ModelRepresentation;
import com.workflow.entity.activiti.exception.BadRequestException;
import com.workflow.entity.activiti.exception.ConflictingRequestException;
import com.workflow.entity.activiti.exception.InternalServerErrorException;
import com.workflow.entity.activiti.exception.NonJsonResourceNotFoundException;
import com.workflow.service.ModelService;
import com.workflow.utils.CollectionUtils;
import com.workflow.utils.SecurityUtils;
import com.workflow.utils.XmlUtils;
import net.sf.json.JSONObject;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

@RestController
public class AppController {
    private static final String RESOLVE_ACTION_OVERWRITE = "overwrite";
    private static final String RESOLVE_ACTION_SAVE_AS = "saveAs";
    private static final String RESOLVE_ACTION_NEW_VERSION = "newVersion";
    public final Logger log = LoggerFactory.getLogger(this.getClass());
    protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
    protected BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ModelService modelService;

    @RequestMapping("/**/editor/i18n")
    public Object getI18nResources() {
        Locale defaultLocale = Locale.getDefault();
        String language = defaultLocale.getLanguage();
        String country = defaultLocale.getCountry();
        String name = "en";
        if ("zh".equals(language)) {
            name = language + "-" + country;
        }
        JSONObject jsonObject = null;
        try {
            File file = ResourceUtils.getFile("editor/i18n/" + name + ".json");
            String input = FileUtils.readFileToString(file, "UTF-8");
            jsonObject = JSONObject.fromObject(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * GET /rest/models/{modelId} -> Get process model
     */
    @RequestMapping(value = "/**/rest/models/{modelId}", method = RequestMethod.GET, produces = "application/json")
    public ModelRepresentation getModel(@PathVariable String modelId) {
        Model model = modelService.getModel(modelId);
        ModelRepresentation result = new ModelRepresentation(model);
        return result;
    }

    /**
     * GET /rest/models/{modelId}/thumbnail -> Get process model thumbnail
     */
    @RequestMapping(value = "/**/rest/models/{modelId}/thumbnail", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getModelThumbnail(@PathVariable String modelId) {
        Model model = modelService.getModel(modelId);
        if (model == null) {
            throw new NonJsonResourceNotFoundException();
        }
        return model.getThumbnail();
    }

    /**
     * PUT /rest/models/{modelId} -> update process model properties
     */
    @RequestMapping(value = "/**/rest/models/{modelId}", method = RequestMethod.PUT)
    public ModelRepresentation updateModel(@PathVariable String modelId, @RequestBody ModelRepresentation updatedModel) {
        // Get model, write-permission required if not a favorite-update
        Model model = modelService.getModel(modelId);

        ModelKeyRepresentation modelKeyInfo = modelService.validateModelKey(model, updatedModel.getModelType(), updatedModel.getKey());
        if (modelKeyInfo.isKeyAlreadyExists()) {
            throw new BadRequestException("Model with provided key already exists " + updatedModel.getKey());
        }
        try {
            updatedModel.updateModel(model);
            modelService.saveModel(model);
            ModelRepresentation result = new ModelRepresentation(model);
            return result;
        } catch (Exception e) {
            throw new BadRequestException("Model cannot be updated: " + modelId);
        }
    }

    /**
     * DELETE /rest/models/{modelId} -> delete process model or, as a non-owner, remove the share info link for that user specifically
     */
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/**/rest/models/{modelId}", method = RequestMethod.DELETE)
    public void deleteModel(@PathVariable String modelId, @RequestParam(required = false) Boolean cascade, @RequestParam(required = false) Boolean deleteRuntimeApp) {
        // Get model to check if it exists, read-permission required for delete (in case user is not owner, only share info
        // will be deleted
        Model model = modelService.getModel(modelId);
        try {
            String currentUserId = SecurityUtils.getCurrentUser();
            boolean currentUserIsOwner = currentUserId.equals(model.getCreatedBy());
            if (currentUserIsOwner) {
                modelService.deleteModel(model.getId(), Boolean.TRUE.equals(cascade), Boolean.TRUE.equals(deleteRuntimeApp));
            }
        } catch (Exception e) {
            log.error("Error while deleting: ", e);
            throw new BadRequestException("Model cannot be deleted: " + modelId);
        }
    }

    /**
     * GET /rest/models/{modelId}/editor/json -> get the JSON model
     * 查看指定modelId对应的流程模型图
     */
    @RequestMapping(value = "/**/rest/models/{modelId}/editor/json", method = RequestMethod.GET, produces = "application/json")
    public ObjectNode getModelJSON(@PathVariable String modelId) {
        Model model = modelService.getModel(modelId);
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put("modelId", model.getId());
        modelNode.put("name", model.getName());
        modelNode.put("key", model.getKey());
        modelNode.put("description", model.getDescription());
        modelNode.putPOJO("lastUpdated", model.getLastUpdated());
        modelNode.put("lastUpdatedBy", model.getLastUpdatedBy());
        String str = model.getModelEditorJson();
//        System.out.println("打印包含中文的内容：" + str);
        if (StringUtils.isNotEmpty(str)) {
            try {
                ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(str);
                editorJsonNode.put("modelType", "model");
                modelNode.set("model", editorJsonNode);
            } catch (Exception e) {
                log.error("Error reading editor json " + modelId, e);
                throw new InternalServerErrorException("Error reading editor json " + modelId);
            }
        } else {
            ObjectNode editorJsonNode = objectMapper.createObjectNode();
            editorJsonNode.put("id", "canvas");
            editorJsonNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorJsonNode.put("modelType", "model");
            modelNode.set("model", editorJsonNode);
        }
        return modelNode;
    }

    /**
     * POST /rest/models/{modelId}/editor/json -> save the JSON model
     */
    @RequestMapping(value = "/**/rest/models/{modelId}/editor/json", method = RequestMethod.POST)
    public ModelRepresentation saveModel(@PathVariable String modelId, @RequestBody MultiValueMap<String, String> values) {
        // Validation: see if there was another update in the meantime
        long lastUpdated = -1L;
        String lastUpdatedString = values.getFirst("lastUpdated");
        if (lastUpdatedString == null) {
            throw new BadRequestException("Missing lastUpdated date");
        }
        try {
            Date readValue = objectMapper.getDeserializationConfig().getDateFormat().parse(lastUpdatedString);
            lastUpdated = readValue.getTime();
        } catch (ParseException e) {
            throw new BadRequestException("Invalid lastUpdated date: '" + lastUpdatedString + "'");
        }
        Model model = modelService.getModel(modelId);
        String currentUser = SecurityUtils.getCurrentUser();
        boolean currentUserIsOwner = model.getLastUpdatedBy().equals(currentUser);
        String resolveAction = values.getFirst("conflictResolveAction");
        // If timestamps differ, there is a conflict or a conflict has been resolved by the user
        if (model.getLastUpdated().getTime() != lastUpdated) {
            if (RESOLVE_ACTION_SAVE_AS.equals(resolveAction)) {
                String saveAs = values.getFirst("saveAs");
                String json = values.getFirst("json_xml");
                return createNewModel(saveAs, model.getDescription(), model.getModelType(), json);
            } else if (RESOLVE_ACTION_OVERWRITE.equals(resolveAction)) {
                return updateModel(model, values, false);
            } else if (RESOLVE_ACTION_NEW_VERSION.equals(resolveAction)) {
                return updateModel(model, values, true);
            } else {
                // Exception case: the user is the owner and selected to create a new version
                String isNewVersionString = values.getFirst("newversion");
                if (currentUserIsOwner && "true".equals(isNewVersionString)) {
                    return updateModel(model, values, true);
                } else {
                    // Tried everything, this is really a conflict, return 409
                    ConflictingRequestException exception = new ConflictingRequestException("Process model was updated in the meantime");
                    exception.addCustomData("userFullName", model.getLastUpdatedBy());
                    exception.addCustomData("newVersionAllowed", currentUserIsOwner);
                    throw exception;
                }
            }
        } else {
            // Actual, regular, update
            return updateModel(model, values, false);
        }
    }

    /**
     * POST /rest/models/{modelId}/editor/newversion -> create a new model version
     */
    @RequestMapping(value = "/**/rest/models/{modelId}/newversion", method = RequestMethod.POST)
    public ModelRepresentation importNewVersion(@PathVariable String modelId, @RequestParam("file") MultipartFile file) {
        Model processModel = modelService.getModel(modelId);
        String currentUser = SecurityUtils.getCurrentUser();
        String fileName = file.getOriginalFilename();
        if (fileName != null && (fileName.endsWith(".bpmn") || fileName.endsWith(".bpmn20.xml"))) {
            try {
                XMLInputFactory xif = XmlUtils.createSafeXmlInputFactory();
                InputStreamReader xmlIn = new InputStreamReader(file.getInputStream(), "UTF-8");
                XMLStreamReader xtr = xif.createXMLStreamReader(xmlIn);
                BpmnModel bpmnModel = bpmnXMLConverter.convertToBpmnModel(xtr);
                if (CollectionUtils.isEmpty(bpmnModel.getProcesses())) {
                    throw new BadRequestException("No process found in definition " + fileName);
                }
                if (bpmnModel.getLocationMap().size() == 0) {
                    throw new BadRequestException("No required BPMN DI information found in definition " + fileName);
                }
                ObjectNode modelNode = bpmnJsonConverter.convertToJson(bpmnModel);
                AbstractModel savedModel = modelService.saveModel(modelId, processModel.getName(), processModel.getKey(),
                        processModel.getDescription(), modelNode.toString(), true, "Version import via REST service", currentUser);
                return new ModelRepresentation(savedModel);
            } catch (BadRequestException e) {
                throw e;
            } catch (Exception e) {
                throw new BadRequestException("Import failed for " + fileName + ", error message " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Invalid file name, only .bpmn and .bpmn20.xml files are supported not " + fileName);
        }
    }

    // 模型再次编辑后保存更新
    protected ModelRepresentation updateModel(Model model, MultiValueMap<String, String> values, boolean forceNewVersion) {
        String name = values.getFirst("name");
        String key = values.getFirst("key");
        String description = values.getFirst("description");
        String isNewVersionString = values.getFirst("newversion");
        String newVersionComment = null;
        ModelKeyRepresentation modelKeyInfo = modelService.validateModelKey(model, model.getModelType(), key);
        if (modelKeyInfo.isKeyAlreadyExists() && "false".equals(isNewVersionString)) {
            throw new BadRequestException("Model with provided key already exists " + key);
        }
        boolean newVersion = false;
        if (forceNewVersion) {
            newVersion = true;
            newVersionComment = values.getFirst("comment");
        } else {
            if (isNewVersionString != null) {
                newVersion = "true".equals(isNewVersionString);
                newVersionComment = values.getFirst("comment");
            }
        }
        String json = values.getFirst("json_xml");
        try {
            model = modelService.saveModel(model.getId(), name, key, description, json, newVersion,
                    newVersionComment, SecurityUtils.getCurrentUser());
            return new ModelRepresentation(model);
        } catch (Exception e) {
            log.error("Error saving model " + model.getId(), e);
            throw new BadRequestException("Process model could not be saved " + model.getId());
        }
    }

    protected ModelRepresentation createNewModel(String name, String description, Integer modelType, String editorJson) {
        ModelRepresentation model = new ModelRepresentation();
        model.setName(name);
        model.setDescription(description);
        model.setModelType(modelType);
        Model newModel = modelService.createModel(model, editorJson, SecurityUtils.getCurrentUser());
        return new ModelRepresentation(newModel);
    }
}
