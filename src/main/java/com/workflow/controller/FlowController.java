package com.workflow.controller;

import com.workflow.entity.*;
import com.workflow.mapper.LeaveMapper;
import com.workflow.mapper.UserRoleMapper;
import com.workflow.service.HistoricVariableService;
import com.workflow.utils.CommonUtils;
import com.workflow.utils.DateUtil;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

@Controller
@RequestMapping("/workflow")
public class FlowController {
    private final String TIME_DELAY = "PT15M";
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private LeaveMapper leaveMapper;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private UserRoleMapper roleMapper;
    @Autowired
    private HistoricVariableService historicVariableService;

    @RequestMapping("")
    public String getPage() {
        return "workflow/index";
    }

    @RequestMapping("/begin")
    public String getStartProcess() {
        return "workflow/start";
    }

    @RequestMapping("/todo")
    public String todo() {
        return "workflow/todo";
    }

    @RequestMapping("/start")
    @ResponseBody
    public String startProcesses(HttpServletRequest request) {
        String key = request.getParameter("key");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String reason = request.getParameter("reason");
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> variables = getParameters(key);
        JSONObject object = new JSONObject();
        identityService.setAuthenticatedUserId(name);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, variables);
        String id = processInstance.getId();
        // 流程定义ID
        String processDefinitionId = processInstance.getProcessDefinitionId();
        // 流程实例ID
        String processInstanceId = processInstance.getProcessInstanceId();
        if ("leave".equals(key)) {
            saveLeaveEntity(reason, endDate, startDate, processInstanceId, name);
        }
        object.put("id", id);
        object.put("processDefinitionId", processDefinitionId);
        return object.toString();
    }

    @RequestMapping("/initiate")
    @ResponseBody
    public void initProcess(HttpServletRequest request) {
        String key = request.getParameter("key");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String reason = request.getParameter("reason");
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> variables = getParameters(key);
        identityService.setAuthenticatedUserId(name);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, variables);
        // 流程实例ID
        String processInstanceId = processInstance.getProcessInstanceId();
        if ("leave".equals(key)) {
            saveLeaveEntity(reason, endDate, startDate, processInstanceId, name);
        }
    }

    private void saveLeaveEntity(String reason, String endDate, String startDate, String processInstanceId, String name) {
        Leave leave = new Leave();
        leave.setReason(reason);
        leave.setEndTime(endDate);
        leave.setStartTime(startDate);
        leave.setApplyTime(DateUtil.getDateTimeFormat(new Date()));
        leave.setProcessInstanceId(processInstanceId);
        leave.setUserId(name);
        leaveMapper.saveLeave(leave);
    }

    private Map<String, Object> getParameters(String key) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> variables = new HashMap<>();
        if ("leave".equalsIgnoreCase(key)) {
            variables.put("applyUserId", name);
        } else {
            variables.put("time1", TIME_DELAY);
            variables.put("time2", TIME_DELAY);
        }
        return variables;
    }

    @RequestMapping("/getVariable")
    @ResponseBody
    public Leave getLeaveDetail(@RequestParam("processInstanceId") String processInstanceId) {
        return leaveMapper.getLeaveInfo(processInstanceId);
    }

    @RequestMapping("/list")
    @ResponseBody
    public DataTableBean getOwnTask(HttpServletRequest request) {
        String draw = request.getParameter("draw");
        String pageSize = request.getParameter("limit");
        String pageStart = request.getParameter("page");
        int limit = Integer.parseInt(pageSize);
        int page = Integer.parseInt(pageStart);
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(name).listPage(page, limit);
        List<Map<String, String>> taskList = new ArrayList<>();
        if (tasks != null && tasks.size() > 0) {
            for (Task task : tasks) {
                Map<String, String> map = new HashMap<>();
                String taskName = task.getName();
                String processInstanceId = task.getProcessInstanceId();
                List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).list();
                if (!CommonUtils.isEmpty(list)) {
                    HistoricProcessInstance historicProcessInstance = list.get(0);
                    String initiator = historicProcessInstance.getStartUserId();
                    String processDefinitionKey = historicProcessInstance.getProcessDefinitionKey();
                    map.put("initiator", initiator);
                    map.put("key", processDefinitionKey);
                }
                String owner = task.getOwner();
                String assignee = task.getAssignee();
                String id = task.getId();
                String processDefinitionId = task.getProcessDefinitionId();
                map.put("taskName", taskName);
                map.put("processDefinitionId", processDefinitionId);
                map.put("owner", owner);
                map.put("id", id);
                map.put("assignee", assignee);
                map.put("processInstanceId", processInstanceId);
                taskList.add(map);
            }
        }
        DataTableBean dtBean = new DataTableBean();
        dtBean.setList(taskList);
        dtBean.setDraw(draw);
        dtBean.setTotal(taskList.size());
        return dtBean;
    }

    // 认领任务
    @RequestMapping("/claim")
    @ResponseBody
    public void claimTask(@RequestParam("taskId") String taskId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        taskService.claim(taskId, userId);
    }

    // 完成个人任务
    @RequestMapping("/complete")
    @ResponseBody
    public void completeTask(@RequestParam("taskId") String taskId, @RequestParam("comment") String comment) {
        // 添加批注时，由于Activiti底层代码使用：
        /*
            String userId = Authentication.getAuthenticatedUserId();
            CommentEntity comment = new CommentEntity();
            comment.setUserId(userId);
            所有需要从Session中获取当前登录人，作为该任务的办理人(审核人)，对应act_hi_comment表中的User_ID字段，
            不过不添加审核人，该字段为null
            所以要求添加配置执行使用Authentication.setAuthenticatedUserId()；添加当前任务的审核人
         */
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Authentication.setAuthenticatedUserId(userId);
        taskService.addComment(taskId, processInstanceId, comment);
        taskService.complete(taskId);
    }

    // 销假
    @RequestMapping("/report")
    @ResponseBody
    public void reportBack(@RequestParam("taskId") String taskId, @RequestParam("backDate") String backDate) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        historicVariableService.deleteVariable(processInstanceId);
        taskService.complete(taskId);
    }

    // 上级领导完成审批任务
    @RequestMapping("/approve")
    @ResponseBody
    public void approveTask(@RequestParam("taskId") String taskId, @RequestParam("result") String result, @RequestParam("key") String key) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processId = task.getProcessDefinitionId();
        String processInstanceId = task.getProcessInstanceId();
        String name = task.getName();
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Authentication.setAuthenticatedUserId(userId);
        if ("emergency".equals(key)) {
            taskService.addComment(taskId, processInstanceId, result);
            taskService.complete(taskId);
        } else if ("leave".equals(key)) {
            // 确定用户角色
            List<UserRole> roleList = roleMapper.getRolesByUserId(userId);
            String role = roleList.get(0).getRole();
            Map<String, Object> variables = new HashMap<>();
            if ("ROLE_DEPTLEADER".equalsIgnoreCase(role)) {
                variables.put("deptLeaderApproved", result);
                if ("true".equals(result)) {
                    leaveMapper.updateLeaderApprovement(processId, result);
                }
            } else if ("ROLE_HR".equalsIgnoreCase(role)) {
                variables.put("hrApproved", result);
                if ("true".equals(result)) {
                    leaveMapper.updateHrApprovement(processId, result);
                }
            }
            HistoricVariable entity = new HistoricVariable();
            entity.setProcessInstanceId(processInstanceId);
            entity.setExecutionId(processInstanceId);
            entity.setTaskId(taskId);
            entity.setName(name);
            entity.setId(UUID.randomUUID().toString());
            entity.setType("string");
            entity.setTextValue(result);
            entity.setCreateTime(DateUtil.getDateTimeFormat(new Date()));
            historicVariableService.saveHistoricVariable(entity);
            taskService.complete(taskId, variables);
        }
    }

    // 重新申请
    @RequestMapping("/reapply")
    @ResponseBody
    public void reapplyTask(@RequestParam("taskId") String taskId, @RequestParam("result") String result) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Authentication.setAuthenticatedUserId(userId);
        Map<String, Object> variables = new HashMap<>();
        variables.put("reApply", result);
        taskService.complete(taskId, variables);
    }

    @RequestMapping("/querycomment")
    public List<Comment> findCommentByTaskId(@RequestParam("taskId") String taskId) {
        // 使用任务ID查询当前任务对象
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // 获取流程实例ID
        String processInstanceId = task.getProcessInstanceId();
        return taskService.getProcessInstanceComments(processInstanceId);
    }

    @RequestMapping("/queryRunningNodes")
    @ResponseBody
    public List<TaskDetail> getNodesUtillNow(HttpServletRequest request) {
        String processInstanceId = request.getParameter("processInstanceId");
        // 当前运行节点
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        // 历史运行节点
        List<HistoricTaskInstance> historyTasks = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).finished().orderByHistoricTaskInstanceEndTime().asc().list();
        List<TaskDetail> returnList = makeTaskDetail(tasks, historyTasks);
        return returnList;
    }

    private List<TaskDetail> makeTaskDetail(List<Task> tasks, List<HistoricTaskInstance> taskInstanceList) {
        List<TaskDetail> taskDetailList = new ArrayList<>();
        for (HistoricTaskInstance historicTask : taskInstanceList) {
            TaskDetail taskDetail = new TaskDetail();
            String taskName = historicTask.getName();
            String userId = historicTask.getAssignee();
            String taskId = historicTask.getId();
            String processInstanceId = historicTask.getProcessInstanceId();
            taskDetail.setName(taskName);
            HistoricVariable historicVariable = null;
//            String taskDefinitionKey = historicTask.getTaskDefinitionKey();
//            List<HistoricVariableInstance> variableInstanceList = null;
//            String varName = getVariableName(taskDefinitionKey);
//            if(varName != null) {
//                 variableInstanceList = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).variableName(varName).list();
//            }
//            if(!CommonUtils.isEmpty(variableInstanceList)){
//                HistoricVariableInstance historicVariableInstance = variableInstanceList.get(0);
//                String result = (String)historicVariableInstance.getValue();
//                taskDetail.setResult(result);
//            }
            historicVariable = historicVariableService.getVariable(processInstanceId, taskId);
            if (historicVariable != null) {
                String type = historicVariable.getType();
                if ("string".equals(type)) {
                    String result = historicVariable.getTextValue();
                    taskDetail.setResult(result);
                }
            }
            String endTime = DateUtil.getDateTimeFormat(historicTask.getEndTime());
            String deleteReason = historicTask.getDeleteReason();
            if (deleteReason != null && deleteReason.indexOf("boundary event") > -1) {
                taskDetail.setStatus(TaskStatus.EXPIRED.toString());
                taskDetail.setResult("--");
            } else {
                taskDetail.setStatus(TaskStatus.FINISHED.toString());
            }
            taskDetail.setUserId(userId);
            taskDetail.setTaskId(taskId);
            taskDetail.setProcessTime(endTime);
            taskDetailList.add(taskDetail);
        }
        for (Task task : tasks) {
            TaskDetail taskDetail = new TaskDetail();
            String currentTaskName = task.getName();
            taskDetail.setName(currentTaskName);
            String assignee = task.getAssignee();
            String taskId = task.getId();
            taskDetail.setTaskId(taskId);
            if (assignee != null) {
                taskDetail.setUserId(assignee);
                taskDetail.setStatus(TaskStatus.PROCESSING.toString());
            } else {
                taskDetail.setStatus(TaskStatus.PENDING.toString());
            }
            taskDetailList.add(taskDetail);
        }
        return taskDetailList;
    }

    private String getVariableName(String taskDefinitionKey) {
        Map<String, String> map = new HashMap<>();
        map.put("hrVerify", "hrApproved");
        map.put("deptLeaderVerify", "deptLeaderApproved");
        map.put("modifyApply", "reApply");
        return map.get(taskDefinitionKey);
    }

    @RequestMapping("/queryNodes")
    @ResponseBody
    public List<TaskDetail> getAllNodes(HttpServletRequest request) {
        String processDefinitionId = request.getParameter("id");
        String processInstanceId = request.getParameter("processInstanceId");
        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
        List<TaskDetail> list = null;
        if (model != null) {
            list = createNodeInfo(model);
        }
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        List<HistoricTaskInstance> historyTasks = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).finished().list();
        List<TaskDetail> returnList = makeTaskDetail(tasks, list, historyTasks);
        return returnList;
    }

    private List<TaskDetail> makeTaskDetail(List<Task> tasks, List<TaskDetail> list, List<HistoricTaskInstance> taskInstanceList) {
        for (TaskDetail taskDetail : list) {
            String name = taskDetail.getName();
            for (Task task : tasks) {
                String currentTaskName = task.getName();
                String assignee = task.getAssignee();
                if (name.equalsIgnoreCase(currentTaskName)) {
                    if (assignee != null) {
                        taskDetail.setUserId(assignee);
                        taskDetail.setStatus(TaskStatus.PROCESSING.toString());
                    } else {
                        taskDetail.setStatus(TaskStatus.PENDING.toString());
                    }
                    break;
                }
            }
            for (HistoricTaskInstance historicTask : taskInstanceList) {
                String taskName = historicTask.getName();
                if (name.equalsIgnoreCase(taskName)) {
                    String userId = historicTask.getAssignee();
                    String endTime = DateUtil.getDateFormat(historicTask.getEndTime());
                    taskDetail.setStatus(TaskStatus.FINISHED.toString());
                    taskDetail.setUserId(userId);
                    taskDetail.setProcessTime(endTime);
                    break;
                }
            }
        }
        return list;
    }

    @RequestMapping("/queryUserTasks")
    @ResponseBody
    public List<Task> getUserTaskNodes(HttpServletRequest request) {
        String processDefinitionId = request.getParameter("id");
        return taskService.createTaskQuery().processDefinitionId(processDefinitionId).list();
    }

    @RequestMapping("/readBpmnFile")
    @ResponseBody
    public List<TaskDetail> getAllNodesFromFile(@RequestParam("name") String modelName) {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xsr = null;
        List<TaskDetail> list = null;
        try {
            File file = ResourceUtils.getFile("/processes/" + modelName + ".bpmn20.xml");
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            xsr = xmlInputFactory.createXMLStreamReader(isr);
            BpmnModel model = new BpmnXMLConverter().convertToBpmnModel(xsr);
            if (model != null) {
                list = createNodeInfo(model);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<TaskDetail> createNodeInfo(BpmnModel model) {
        List<TaskDetail> list = new ArrayList<>();
        Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof UserTask) {
                UserTask userTask = (UserTask) flowElement;
                TaskDetail taskDetail = new TaskDetail();
                String name = userTask.getName();
                String id = userTask.getId();
                List<String> candidates = userTask.getCandidateGroups();
                String assignee = userTask.getAssignee();
                String userId = null;
                if (assignee == null) {
                    userId = String.join(",", candidates);
                } else if (assignee.indexOf("${") > -1) {
                    userId = SecurityContextHolder.getContext().getAuthentication().getName();
                } else {
                    userId = assignee;
                }
                taskDetail.setName(name);
                taskDetail.setTaskId(id);
                taskDetail.setUserId(userId);
                list.add(taskDetail);
            }
        }
        return list;
    }

    @RequestMapping("/queryApp")
    @ResponseBody
    public List<Map<String, String>> getProcessInfo() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Map<String, String>> list = new ArrayList<>();
        List<HistoricProcessInstance> historicProcessInstanceList = historyService.createHistoricProcessInstanceQuery().startedBy(userId).list();
        for (HistoricProcessInstance historicProcessInstance : historicProcessInstanceList) {
            Map<String, String> map = new HashMap<>();
            String processDefinitionId = historicProcessInstance.getProcessDefinitionId();
            String name = historicProcessInstance.getProcessDefinitionName();
            String startTime = DateUtil.getDateFormat(historicProcessInstance.getStartTime());
            String endActivityId = historicProcessInstance.getEndActivityId();
            String processInstanceId = historicProcessInstance.getId();
            List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).active().list();
            if (!CommonUtils.isEmpty(instanceList)) {
                map.put("state", "running");
            } else {
                if (endActivityId != null) {
                    map.put("state", "done");
                } else {
                    map.put("state", "suspended");
                }
            }
            map.put("name", name);
            map.put("processDefinitionId", processDefinitionId);
            map.put("startTime", startTime);
            map.put("processInstanceId", processInstanceId);
            list.add(map);
        }
        return list;
    }

    @RequestMapping("/suspend")
    @ResponseBody
    public void suspendProcess(@RequestParam("processInstanceId") String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    @RequestMapping("/activate")
    @ResponseBody
    public void activateProcess(@RequestParam("processInstanceId") String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
    }
}