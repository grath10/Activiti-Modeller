package com.workflow.config;

import com.workflow.entity.CustomGroupEntityManager;
import com.workflow.mapper.UserRoleMapper;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class)
public class ActivitiConfig implements ProcessEngineConfigurationConfigurer {
    @Autowired
    private UserRoleMapper roleMapper;

    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
        processEngineConfiguration.setDbIdentityUsed(false);
        CustomGroupEntityManager groupEntityManager = new CustomGroupEntityManager(processEngineConfiguration, roleMapper);
        processEngineConfiguration.setGroupDataManager(groupEntityManager);
        processEngineConfiguration.setActivityFontName("宋体");
        processEngineConfiguration.setLabelFontName("宋体");
        processEngineConfiguration.setAnnotationFontName("宋体");
    }
}
