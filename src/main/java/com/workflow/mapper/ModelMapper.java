package com.workflow.mapper;

import com.workflow.entity.activiti.Model;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ModelMapper {
    List<Model> findModelsCreatedBy(@Param("user") String createdBy, @Param("modelType") Integer modelType);

    List<Model> findModelsCreatedByFilter(@Param("user") String createdBy, @Param("modelType") Integer modelType, @Param("filter") String filter);

    List<Model> findModelsByKeyAndType(@Param("key") String key, @Param("modelType") Integer modelType);

    List<Model> findModelsByModelTypeFilter(@Param("modelType") Integer modelType, @Param("filter") String filter);

    List<Model> findModelsByModelType(@Param("modelType") Integer modelType);

    Long countByModelTypeAndUser(@Param("modelType") int modelType, @Param("user") String user);

    Model findOne(String modelId);

    String appDefinitionIdByModelAndUser(@Param("modelId") String modelId, @Param("user") String user);

    int save(Model model);
}
