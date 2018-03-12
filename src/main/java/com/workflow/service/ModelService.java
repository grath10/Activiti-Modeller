package com.workflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.workflow.entity.activiti.*;
import com.workflow.entity.activiti.exception.InternalServerErrorException;
import com.workflow.mapper.ModelHistoryMapper;
import com.workflow.mapper.ModelMapper;
import com.workflow.utils.CollectionUtils;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ModelService {
    public static final String NAMESPACE = "http://activiti.com/modeler";
    protected static final String PROCESS_NOT_FOUND_MESSAGE_KEY = "PROCESS.ERROR.NOT-FOUND";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ModelImageService modelImageService;
    @Autowired
    private ModelHistoryMapper modelHistoryMapper;

    protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
    protected BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();

    public Model getModel(String modelId) {
        Model model = modelMapper.findOne(modelId);
        if (model == null) {
            NotFoundException modelNotFound = new NotFoundException("No model found with the given id: " + modelId);
            modelNotFound.setMessageKey(PROCESS_NOT_FOUND_MESSAGE_KEY);
            throw modelNotFound;
        }
        return model;
    }

    public List<AbstractModel> getModelsByModelType(Integer modelType) {
        return new ArrayList<AbstractModel>(modelMapper.findModelsByModelType(modelType));
    }

    public ModelHistory getModelHistory(String modelId, String modelHistoryId) {
        // Check if the user has read-rights on the process-model in order to fetch history
        Model model = getModel(modelId);
        ModelHistory modelHistory = modelHistoryMapper.findOne(modelHistoryId);

        // Check if history corresponds to the current model and is not deleted
        if (modelHistory == null || modelHistory.getRemovalDate() != null || !modelHistory.getModelId().equals(model.getId())) {
            throw new NotFoundException("Process model history not found: " + modelHistoryId);
        }
        return modelHistory;
    }

    public byte[] getBpmnXML(AbstractModel model) {
        BpmnModel bpmnModel = getBpmnModel(model);
        return getBpmnXML(bpmnModel);
    }

    public byte[] getBpmnXML(BpmnModel bpmnModel) {
        for (Process process : bpmnModel.getProcesses()) {
            if (StringUtils.isNotEmpty(process.getId())) {
                char firstCharacter = process.getId().charAt(0);
                // no digit is allowed as first character
                if (Character.isDigit(firstCharacter)) {
                    process.setId("a" + process.getId());
                }
            }
        }
        byte[] xmlBytes = bpmnXMLConverter.convertToXML(bpmnModel);
        return xmlBytes;
    }

    public ModelKeyRepresentation validateModelKey(Model model, Integer modelType, String key) {
        ModelKeyRepresentation modelKeyResponse = new ModelKeyRepresentation();
        modelKeyResponse.setKey(key);
        List<Model> models = modelMapper.findModelsByKeyAndType(key, modelType);
        for (Model modelInfo : models) {
            if (model == null || modelInfo.getId().equals(model.getId()) == false) {
                modelKeyResponse.setKeyAlreadyExists(true);
                modelKeyResponse.setId(modelInfo.getId());
                modelKeyResponse.setName(modelInfo.getName());
                break;
            }
        }
        return modelKeyResponse;
    }

    public Model createModel(Model newModel, String createdBy) {
        newModel.setVersion(1);
        newModel.setCreated(Calendar.getInstance().getTime());
        newModel.setCreatedBy(createdBy);
        newModel.setLastUpdated(Calendar.getInstance().getTime());
        newModel.setLastUpdatedBy(createdBy);
        newModel = persistModel(newModel);
        return newModel;
    }

    public Model createModel(ModelRepresentation model, String editorJson, String createdBy) {
        Model newModel = new Model();
        newModel.setVersion(1);
        newModel.setName(model.getName());
        newModel.setKey(model.getKey());
        newModel.setModelType(model.getModelType());
        newModel.setCreated(Calendar.getInstance().getTime());
        newModel.setCreatedBy(createdBy);
        newModel.setDescription(model.getDescription());
        newModel.setModelEditorJson(editorJson);
        newModel.setLastUpdated(Calendar.getInstance().getTime());
        newModel.setLastUpdatedBy(createdBy);
        newModel = persistModel(newModel);
        return newModel;
    }

    public Model createNewModelVersion(Model modelObject, String comment, String updatedBy) {
        return (Model) internalCreateNewModelVersion(modelObject, comment, updatedBy, false);
    }

    public ModelHistory createNewModelVersionAndReturnModelHistory(Model modelObject, String comment, String updatedBy) {
        return (ModelHistory) internalCreateNewModelVersion(modelObject, comment, updatedBy, true);
    }

    protected AbstractModel internalCreateNewModelVersion(Model modelObject, String comment, String updatedBy, boolean returnModelHistory) {
        modelObject.setLastUpdated(new Date());
        modelObject.setLastUpdatedBy(updatedBy);
        modelObject.setComment(comment);

        ModelHistory historyModel = createNewModelhistory(modelObject);
        persistModelHistory(historyModel);

        modelObject.setVersion(modelObject.getVersion() + 1);
        persistModel(modelObject);

        return returnModelHistory ? historyModel : modelObject;
    }

    public Model saveModel(Model modelObject) {
        return persistModel(modelObject);
    }

    public Model saveModel(Model modelObject, String editorJson, byte[] imageBytes, boolean newVersion, String newVersionComment, String updatedBy) {
        return internalSave(modelObject.getName(), modelObject.getKey(), modelObject.getDescription(), editorJson, newVersion,
                newVersionComment, imageBytes, updatedBy, modelObject);
    }

    public Model saveModel(String modelId, String name, String key, String description, String editorJson,
                           boolean newVersion, String newVersionComment, String updatedBy) {
        Model modelObject = modelMapper.findOne(modelId);
        return internalSave(name, key, description, editorJson, newVersion, newVersionComment, null, updatedBy, modelObject);
    }

    protected Model internalSave(String name, String key, String description, String editorJson, boolean newVersion,
                                 String newVersionComment, byte[] imageBytes, String updatedBy, Model modelObject) {
        if (newVersion == false) {
            modelObject.setLastUpdated(new Date());
            modelObject.setLastUpdatedBy(updatedBy);
            modelObject.setName(name);
            modelObject.setKey(key);
            modelObject.setDescription(description);
            modelObject.setModelEditorJson(editorJson);
            if (imageBytes != null) {
                modelObject.setThumbnail(imageBytes);
            }
        } else {
            ModelHistory historyModel = createNewModelhistory(modelObject);
            persistModelHistory(historyModel);
            modelObject.setVersion(modelObject.getVersion() + 1);
            modelObject.setLastUpdated(new Date());
            modelObject.setLastUpdatedBy(updatedBy);
            modelObject.setName(name);
            modelObject.setKey(key);
            modelObject.setDescription(description);
            modelObject.setModelEditorJson(editorJson);
            modelObject.setComment(newVersionComment);
            if (imageBytes != null) {
                modelObject.setThumbnail(imageBytes);
            }
        }
        return persistModel(modelObject);
    }

    public void deleteModel(String modelId, boolean cascadeHistory, boolean deleteRuntimeApp) {
        Model model = modelMapper.findOne(modelId);
        if (model == null) {
            throw new IllegalArgumentException("No model found with id: " + modelId);
        }

        // Fetch current model history list
        List<ModelHistory> history = modelHistoryMapper.findByModelIdAndRemovalDateIsNullOrderByVersionDesc(model.getId());

        // Move model to history and mark removed
        ModelHistory historyModel = createNewModelhistory(model);
        historyModel.setRemovalDate(Calendar.getInstance().getTime());
        persistModelHistory(historyModel);

        // History available and no cascade was requested. Revive latest history entry
        ModelHistory toRevive = history.remove(0);
        populateModelBasedOnHistory(model, toRevive);
        persistModel(model);
        modelHistoryMapper.delete(toRevive);
    }

    /*protected void deleteModelAndChildren(Model model) {

        // Models have relations with each other, in all kind of wicked and funny ways.
        // Hence, we remove first all relations, comments, etc. while collecting all models.
        // Then, once all foreign key problemmakers are removed, we remove the models

        List<Model> allModels = new ArrayList<Model>();
        internalDeleteModelAndChildren(model, allModels);

        for (Model modelToDelete : allModels) {
            modelRepository.delete(modelToDelete);
        }
    }

    protected void internalDeleteModelAndChildren(Model model, List<Model> allModels) {
        // Delete all related data
        modelRelationRepository.deleteModelRelationsForParentModel(model.getId());
        allModels.add(model);
    }*/

    public ReviveModelResultRepresentation reviveProcessModelHistory(ModelHistory modelHistory, String user, String newVersionComment) {
        Model latestModel = modelMapper.findOne(modelHistory.getModelId());
        if (latestModel == null) {
            throw new IllegalArgumentException("No process model found with id: " + modelHistory.getModelId());
        }
        // Store the current model in history
        ModelHistory latestModelHistory = createNewModelhistory(latestModel);
        persistModelHistory(latestModelHistory);
        // Populate the actual latest version with the properties in the historic model
        latestModel.setVersion(latestModel.getVersion() + 1);
        latestModel.setLastUpdated(new Date());
        latestModel.setLastUpdatedBy(user);
        latestModel.setName(modelHistory.getName());
        latestModel.setKey(modelHistory.getKey());
        latestModel.setDescription(modelHistory.getDescription());
        latestModel.setModelEditorJson(modelHistory.getModelEditorJson());
        latestModel.setModelType(modelHistory.getModelType());
        latestModel.setComment(newVersionComment);
        persistModel(latestModel);

        ReviveModelResultRepresentation result = new ReviveModelResultRepresentation();
        return result;
    }

    public BpmnModel getBpmnModel(AbstractModel model) {
        BpmnModel bpmnModel = null;
        try {
            Map<String, Model> formMap = new HashMap<String, Model>();
            Map<String, Model> decisionTableMap = new HashMap<String, Model>();

            /*List<Model> referencedModels = modelMapper.findModelsByParentModelId(model.getId());
            for (Model childModel : referencedModels) {
                if (Model.MODEL_TYPE_FORM == childModel.getModelType()) {
                    formMap.put(childModel.getId(), childModel);

                } else if (Model.MODEL_TYPE_DECISION_TABLE == childModel.getModelType()) {
                    decisionTableMap.put(childModel.getId(), childModel);
                }
            }*/

            bpmnModel = getBpmnModel(model, formMap, decisionTableMap);
        } catch (Exception e) {
            log.error("Could not generate BPMN 2.0 model for " + model.getId(), e);
            throw new InternalServerErrorException("Could not generate BPMN 2.0 model");
        }

        return bpmnModel;
    }

    public BpmnModel getBpmnModel(AbstractModel model, Map<String, Model> formMap, Map<String, Model> decisionTableMap) {
        try {
            ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(model.getModelEditorJson());
            Map<String, String> formKeyMap = new HashMap<String, String>();
            for (Model formModel : formMap.values()) {
                formKeyMap.put(formModel.getId(), formModel.getKey());
            }
            Map<String, String> decisionTableKeyMap = new HashMap<String, String>();
            for (Model decisionTableModel : decisionTableMap.values()) {
                decisionTableKeyMap.put(decisionTableModel.getId(), decisionTableModel.getKey());
            }
            return bpmnJsonConverter.convertToBpmnModel(editorJsonNode, formKeyMap, decisionTableKeyMap);
        } catch (Exception e) {
            log.error("Could not generate BPMN 2.0 model for " + model.getId(), e);
            throw new InternalServerErrorException("Could not generate BPMN 2.0 model");
        }
    }

    protected void addOrUpdateExtensionElement(String name, String value, UserTask userTask) {
        List<ExtensionElement> extensionElements = userTask.getExtensionElements().get(name);
        ExtensionElement extensionElement;

        if (CollectionUtils.isNotEmpty(extensionElements)) {
            extensionElement = extensionElements.get(0);
        } else {
            extensionElement = new ExtensionElement();
        }
        extensionElement.setNamespace(NAMESPACE);
        extensionElement.setNamespacePrefix("modeler");
        extensionElement.setName(name);
        extensionElement.setElementText(value);

        if (CollectionUtils.isEmpty(extensionElements)) {
            userTask.addExtensionElement(extensionElement);
        }
    }

    public Long getModelCountForUser(User user, int modelType) {
        return modelMapper.countByModelTypeAndUser(modelType, user.getId());
    }

    protected Model persistModel(Model model) {
        String str = model.getModelEditorJson();
        if (StringUtils.isNotEmpty(str)) {
            // Parse json to java
            ObjectNode jsonNode = null;
            try {
                jsonNode = (ObjectNode) objectMapper.readTree(str);
            } catch (Exception e) {
                log.error("Could not deserialize json model", e);
                modelMapper.save(model);
                throw new InternalServerErrorException("Could not deserialize json model");
            }
            if ((model.getModelType() == null || model.getModelType().intValue() == Model.MODEL_TYPE_BPMN)) {
                // Thumbnail
                Model newModel = modelImageService.generateThumbnailImage(model, jsonNode);
                modelMapper.save(newModel);
            }
        } else {
            modelMapper.save(model);
        }
        List<Model> models = modelMapper.findModelsByKeyAndType(model.getKey(), model.getModelType());
        if(CollectionUtils.isNotEmpty(models)){
            model = models.get(0);
        }
        return model;
    }

    protected ModelHistory persistModelHistory(ModelHistory modelHistory) {
        modelHistoryMapper.save(modelHistory);
        return modelHistoryMapper.findOne(modelHistory.getModelId());
    }

    /*protected void handleBpmnProcessFormModelRelations(AbstractModel bpmnProcessModel, ObjectNode editorJsonNode) {
        List<JsonNode> formReferenceNodes = JsonConverterUtil.filterOutJsonNodes(JsonConverterUtil.getBpmnProcessModelFormReferences(editorJsonNode));
        Set<String> formIds = JsonConverterUtil.gatherStringPropertyFromJsonNodes(formReferenceNodes, "id");

        handleModelRelations(bpmnProcessModel, formIds, ModelRelationTypes.TYPE_FORM_MODEL_CHILD);
    }

    protected void handleBpmnProcessDecisionTaskModelRelations(AbstractModel bpmnProcessModel, ObjectNode editorJsonNode) {
        List<JsonNode> decisionTableNodes = JsonConverterUtil.filterOutJsonNodes(JsonConverterUtil.getBpmnProcessModelDecisionTableReferences(editorJsonNode));
        Set<String> decisionTableIds = JsonConverterUtil.gatherStringPropertyFromJsonNodes(decisionTableNodes, "id");

        handleModelRelations(bpmnProcessModel, decisionTableIds, ModelRelationTypes.TYPE_DECISION_TABLE_MODEL_CHILD);
    }

    protected void handleAppModelProcessRelations(AbstractModel appModel, ObjectNode appModelJsonNode) {
        Set<String> processModelIds = JsonConverterUtil.getAppModelReferencedModelIds(appModelJsonNode);
        handleModelRelations(appModel, processModelIds, ModelRelationTypes.TYPE_PROCESS_MODEL);
    }*/

    /**
     * Generic handling of model relations: deleting/adding where needed.
     */
    /*protected void handleModelRelations(AbstractModel bpmnProcessModel, Set<String> idsReferencedInJson, String relationshipType) {
        // Find existing persisted relations
        List<ModelRelation> persistedModelRelations = modelRelationRepository.findByParentModelIdAndType(bpmnProcessModel.getId(), relationshipType);

        // if no ids referenced now, just delete them all
        if (idsReferencedInJson == null || idsReferencedInJson.size() == 0) {
            modelRelationRepository.delete(persistedModelRelations);
            return;
        }

        Set<String> alreadyPersistedModelIds = new HashSet<String>(persistedModelRelations.size());
        for (ModelRelation persistedModelRelation : persistedModelRelations) {
            if (!idsReferencedInJson.contains(persistedModelRelation.getModelId())) {
                // model used to be referenced, but not anymore. Delete it.
                modelRelationRepository.delete((ModelRelation) persistedModelRelation);
            } else {
                alreadyPersistedModelIds.add(persistedModelRelation.getModelId());
            }
        }

        // Loop over all referenced ids and see which one are new
        for (String idReferencedInJson : idsReferencedInJson) {
            // if model is referenced, but it is not yet persisted = create it
            if (!alreadyPersistedModelIds.contains(idReferencedInJson)) {
                // Check if model actually still exists. Don't create the relationship if it doesn't exist. The client UI will have cope with this too.
                if (modelRepository.exists(idReferencedInJson)) {
                    modelRelationRepository.save(new ModelRelation(bpmnProcessModel.getId(), idReferencedInJson, relationshipType));
                }
            }
        }
    }*/

    protected ModelHistory createNewModelhistory(Model model) {
        ModelHistory historyModel = new ModelHistory();
        historyModel.setName(model.getName());
        historyModel.setKey(model.getKey());
        historyModel.setDescription(model.getDescription());
        historyModel.setCreated(model.getCreated());
        historyModel.setLastUpdated(model.getLastUpdated());
        historyModel.setCreatedBy(model.getCreatedBy());
        historyModel.setLastUpdatedBy(model.getLastUpdatedBy());
        historyModel.setModelEditorJson(model.getModelEditorJson());
        historyModel.setModelType(model.getModelType());
        historyModel.setVersion(model.getVersion());
        historyModel.setModelId(model.getId());
        historyModel.setComment(model.getComment());
        return historyModel;
    }

    protected void populateModelBasedOnHistory(Model model, ModelHistory basedOn) {
        model.setName(basedOn.getName());
        model.setKey(basedOn.getKey());
        model.setDescription(basedOn.getDescription());
        model.setCreated(basedOn.getCreated());
        model.setLastUpdated(basedOn.getLastUpdated());
        model.setCreatedBy(basedOn.getCreatedBy());
        model.setLastUpdatedBy(basedOn.getLastUpdatedBy());
        model.setModelEditorJson(basedOn.getModelEditorJson());
        model.setModelType(basedOn.getModelType());
        model.setVersion(basedOn.getVersion());
        model.setComment(basedOn.getComment());
    }
}
