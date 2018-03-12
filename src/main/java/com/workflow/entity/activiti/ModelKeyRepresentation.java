package com.workflow.entity.activiti;

public class ModelKeyRepresentation {
    protected boolean keyAlreadyExists;
    protected String key;
    protected String id;
    protected String name;

    public boolean isKeyAlreadyExists() {
        return keyAlreadyExists;
    }

    public void setKeyAlreadyExists(boolean keyAlreadyExists) {
        this.keyAlreadyExists = keyAlreadyExists;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
