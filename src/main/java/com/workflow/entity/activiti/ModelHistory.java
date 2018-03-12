package com.workflow.entity.activiti;

import java.util.Date;

public class ModelHistory extends AbstractModel {
    protected String modelId;
    protected Date removalDate;

    public ModelHistory() {
        super();
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Date getRemovalDate() {
        return removalDate;
    }

    public void setRemovalDate(Date removalDate) {
        this.removalDate = removalDate;
    }
}
