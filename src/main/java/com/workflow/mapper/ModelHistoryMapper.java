package com.workflow.mapper;

import com.workflow.entity.activiti.ModelHistory;

import java.util.List;

public interface ModelHistoryMapper {
    List<ModelHistory> findByCreatedByAndModelTypeAndRemovalDateIsNull(String createdBy, Integer modelType);

    List<ModelHistory> findByModelIdAndRemovalDateIsNullOrderByVersionDesc(String modelId);

    List<ModelHistory> findByModelIdOrderByVersionDesc(Long modelId);

    int delete(ModelHistory modelHistory);

    ModelHistory findOne(String modelHistoryId);

    int save(ModelHistory modelHistory);
}
