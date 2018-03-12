package com.workflow.controller;

import com.workflow.entity.UserRole;
import com.workflow.entity.activiti.ResultListDataRepresentation;
import com.workflow.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EditorGroupsController {
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/**/rest/editor-groups", method = RequestMethod.GET)
    public ResultListDataRepresentation getGroups(@RequestParam(required = false, value = "filter") String filter) {
        String groupNameFilter = filter;
        if (StringUtils.isEmpty(groupNameFilter)) {
            groupNameFilter = "%";
        } else {
            groupNameFilter = "%" + groupNameFilter + "%";
        }
        List<UserRole> roleList = userService.getMatchingRoles(groupNameFilter);
        ResultListDataRepresentation result = new ResultListDataRepresentation(roleList);
        return result;
    }
}
