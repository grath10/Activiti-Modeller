package com.workflow.entity.activiti.exception;

public class InternalServerErrorException extends BaseModelerRestException {

    private static final long serialVersionUID = 1L;

    public InternalServerErrorException() {
    }

    public InternalServerErrorException(String message) {
        super(message);
    }

    public InternalServerErrorException(String message, Throwable t) {
        super(message, t);
    }
}
