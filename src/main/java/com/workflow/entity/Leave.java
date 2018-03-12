package com.workflow.entity;

public class Leave {
    private long id;
    private String processInstanceId;
    private String userId;
    private String startTime;
    private String endTime;
    private String realStartTime;
    private String realEndTime;
    private String applyTime;
    private String reason;
    private String deptApproved;
    private String hrApproved;

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRealStartTime() {
        return realStartTime;
    }

    public void setRealStartTime(String realStartTime) {
        this.realStartTime = realStartTime;
    }

    public String getRealEndTime() {
        return realEndTime;
    }

    public void setRealEndTime(String realEndTime) {
        this.realEndTime = realEndTime;
    }

    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDeptApproved() {
        return deptApproved;
    }

    public void setDeptApproved(String deptApproved) {
        this.deptApproved = deptApproved;
    }

    public String getHrApproved() {
        return hrApproved;
    }

    public void setHrApproved(String hrApproved) {
        this.hrApproved = hrApproved;
    }
}
