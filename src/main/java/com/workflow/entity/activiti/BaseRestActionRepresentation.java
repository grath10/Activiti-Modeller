package com.workflow.entity.activiti;

public class BaseRestActionRepresentation {
    protected String action;
    protected String comment;

    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
}
