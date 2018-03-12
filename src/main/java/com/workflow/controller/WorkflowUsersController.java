package com.workflow.controller;

import com.github.pagehelper.PageHelper;
import com.workflow.entity.SysUser;
import com.workflow.entity.activiti.ResultListDataRepresentation;
import com.workflow.service.UserService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.IdentityLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@RestController
public class WorkflowUsersController {
    private static final int MAX_PEOPLE_SIZE = 50;
    @Autowired
    private UserService userService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    @RequestMapping(value = "/**/rest/workflow-users", method = RequestMethod.GET)
    public ResultListDataRepresentation getUsers(@RequestParam(value = "filter", required = false) String filter,
                                                 @RequestParam(value = "email", required = false) String email,
                                                 @RequestParam(value = "externalId", required = false) String externalId,
                                                 @RequestParam(value = "excludeTaskId", required = false) String excludeTaskId,
                                                 @RequestParam(value = "excludeProcessId", required = false) String excludeProcessId,
                                                 @RequestParam(value = "groupId", required = false) Long groupId,
                                                 @RequestParam(value = "tenantId", required = false) Long tenantId) {
        // Actual query
        int page = 0;
        int pageSize = MAX_PEOPLE_SIZE;

        PageHelper.startPage(page, pageSize);
        List<SysUser> matchingUsers = userService.findFuzzyUsers(filter);

        // Filter out users already part of the task/process of which the ID has been passed
        if (excludeTaskId != null) {
            filterUsersInvolvedInTask(excludeTaskId, matchingUsers);
        } else if (excludeProcessId != null) {
            filterUsersInvolvedInProcess(excludeProcessId, matchingUsers);
        }

        ResultListDataRepresentation result = new ResultListDataRepresentation(matchingUsers);

        if (page != 0 || (page == 0 && matchingUsers.size() == pageSize)) {
            // Total differs from actual result size, need to fetch it
            result.setTotal((long) matchingUsers.size());
        }
        return result;
    }

    protected void filterUsersInvolvedInProcess(String excludeProcessId, List<SysUser> matchingUsers) {
        Set<String> involvedUsers = getInvolvedUsersAsSet(runtimeService.getIdentityLinksForProcessInstance(excludeProcessId));
        removeinvolvedUsers(matchingUsers, involvedUsers);
    }

    protected void filterUsersInvolvedInTask(String excludeTaskId, List<SysUser> matchingUsers) {
        Set<String> involvedUsers = getInvolvedUsersAsSet(taskService.getIdentityLinksForTask(excludeTaskId));
        removeinvolvedUsers(matchingUsers, involvedUsers);
    }

    protected Set<String> getInvolvedUsersAsSet(List<IdentityLink> involvedPeople) {
        Set<String> involved = null;
        if (involvedPeople.size() > 0) {
            involved = new HashSet<>();
            for (IdentityLink link : involvedPeople) {
                if (link.getUserId() != null) {
                    involved.add(link.getUserId());
                }
            }
        }
        return involved;
    }

    protected void removeinvolvedUsers(List<SysUser> matchingUsers, Set<String> involvedUsers) {
        if (involvedUsers != null) {
            // Using iterator to be able to remove without ConcurrentModExceptions
            Iterator<SysUser> userIt = matchingUsers.iterator();
            while (userIt.hasNext()) {
                if (involvedUsers.contains(userIt.next().getId())) {
                    userIt.remove();
                }
            }
        }
    }
}
