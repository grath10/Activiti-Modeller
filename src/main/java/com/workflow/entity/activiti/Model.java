package com.workflow.entity.activiti;

public class Model extends AbstractModel {
    private byte[] thumbnail;

    public Model() {
        super();
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }
}
