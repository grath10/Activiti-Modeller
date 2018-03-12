package com.workflow.entity.activiti;

import com.workflow.entity.activiti.exception.BaseModelerRestException;

public class NotFoundException extends BaseModelerRestException {
    private static final long serialVersionUID = 1L;

    public NotFoundException() {
    }

    public NotFoundException(String s) {
        super(s);
    }
}
