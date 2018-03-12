package com.workflow.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.entity.activiti.exception.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
public class StencilSetController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected ObjectMapper objectMapper;

    @RequestMapping(value = "/**/rest/stencil-sets/editor", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getStencilSetForEditor() {
        Locale defaultLocale = Locale.getDefault();
        String language = defaultLocale.getLanguage();
        try {
            JsonNode stencilNode = objectMapper.readTree(this.getClass().getClassLoader().getResourceAsStream("stencilset_bpmn_" + language + ".json"));
            return stencilNode;
        } catch (Exception e) {
            log.error("Error reading bpmn stencil set json", e);
            throw new InternalServerErrorException("Error reading bpmn stencil set json");
        }
    }
}
