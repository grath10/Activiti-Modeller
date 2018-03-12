package com.workflow.mapper;

import com.workflow.entity.activiti.*;
import org.activiti.bpmn.model.BpmnModel;

import java.util.List;
import java.util.Map;

public interface ActivitiModelMapper {
    Model getModel(String modelId);

    List<AbstractModel> getModelsByModelType(Integer modelType);

    ModelKeyRepresentation validateModelKey(Model model, Integer modelType, String key);

    ModelHistory getModelHistory(String modelId, String modelHistoryId);

    Long getModelCountForUser(String user, int modelTypeApp);

    BpmnModel getBpmnModel(AbstractModel model);

    byte[] getBpmnXML(BpmnModel bpmnMode);

    byte[] getBpmnXML(AbstractModel model);

    BpmnModel getBpmnModel(AbstractModel model, Map<String, Model> formMap, Map<String, Model> decisionTableMap);

    Model createModel(ModelRepresentation model, String editorJson, String createdBy);

    Model createModel(Model newModel, String createdBy);

    Model saveModel(Model modelObject);

    Model saveModel(Model modelObject, String editorJson, byte[] imageBytes, boolean newVersion, String newVersionComment, String updatedBy);

    Model saveModel(String modelId, String name, String key, String description, String editorJson,
                    boolean newVersion, String newVersionComment, String updatedBy);

    Model createNewModelVersion(Model modelObject, String comment, String updatedBy);

    ModelHistory createNewModelVersionAndReturnModelHistory(Model modelObject, String comment, String updatedBy);

    void deleteModel(String modelId, boolean cascadeHistory, boolean deleteRuntimeApp);

    ReviveModelResultRepresentation reviveProcessModelHistory(ModelHistory modelHistory, String user, String newVersionComment);
}
