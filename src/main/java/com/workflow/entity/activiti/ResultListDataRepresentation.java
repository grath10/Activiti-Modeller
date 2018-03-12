package com.workflow.entity.activiti;

import java.util.List;

public class ResultListDataRepresentation {
    protected Integer size;
    protected Long total;
    protected Integer start;
    protected List<? extends Object> data;

    public ResultListDataRepresentation() {
    }

    public ResultListDataRepresentation(List<? extends Object> data) {
        this.data = data;
        if (data != null) {
            size = data.size();
            total = Long.valueOf(data.size());
            start = 0;
        }
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public List<? extends Object> getData() {
        return data;
    }

    public void setData(List<? extends Object> data) {
        this.data = data;
    }
}
