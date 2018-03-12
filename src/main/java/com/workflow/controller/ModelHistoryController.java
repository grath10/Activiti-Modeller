package com.workflow.controller;

import com.workflow.entity.activiti.*;
import com.workflow.entity.activiti.exception.BadRequestException;
import com.workflow.mapper.ModelHistoryMapper;
import com.workflow.service.ModelService;
import com.workflow.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ModelHistoryController {
    @Autowired
    private ModelService modelService;
    @Autowired
    private ModelHistoryMapper modelHistoryMapper;

    @RequestMapping(value = "/**/rest/models/{modelId}/history", method = RequestMethod.GET, produces = "application/json")
    public ResultListDataRepresentation getModelHistoryCollection(@PathVariable String modelId, @RequestParam(value = "includeLatestVersion", required = false) Boolean includeLatestVersion) {
        Model model = modelService.getModel(modelId);
        List<ModelHistory> history = modelHistoryMapper.findByModelIdAndRemovalDateIsNullOrderByVersionDesc(model.getId());
        ResultListDataRepresentation result = new ResultListDataRepresentation();

        List<ModelRepresentation> representations = new ArrayList<>();

        // Also include the latest version of the model
        if (Boolean.TRUE.equals(includeLatestVersion)) {
            representations.add(new ModelRepresentation(model));
        }
        if (history.size() > 0) {
            for (ModelHistory modelHistory : history) {
                representations.add(new ModelRepresentation(modelHistory));
            }
            result.setData(representations);
        }

        // Set size and total
        result.setSize(representations.size());
        result.setTotal(Long.valueOf(representations.size()));
        result.setStart(0);
        return result;
    }

    @RequestMapping(value = "/**/rest/models/{modelId}/history/{modelHistoryId}", method = RequestMethod.GET, produces = "application/json")
    public ModelRepresentation getProcessModelHistory(@PathVariable String modelId, @PathVariable String modelHistoryId) {
        // Check if the user has read-rights on the process-model in order to fetch history
        ModelHistory modelHistory = modelService.getModelHistory(modelId, modelHistoryId);
        return new ModelRepresentation(modelHistory);
    }

    @RequestMapping(value = "/**/rest/models/{modelId}/history/{modelHistoryId}", method = RequestMethod.POST, produces = "application/json")
    public ReviveModelResultRepresentation executeProcessModelHistoryAction(@PathVariable String modelId, @PathVariable String modelHistoryId,
                                                                            @RequestBody(required = true) BaseRestActionRepresentation action) {
        // In order to execute actions on a historic process model, write permission is needed
        ModelHistory modelHistory = modelService.getModelHistory(modelId, modelHistoryId);
        if ("useAsNewVersion".equals(action.getAction())) {
            return modelService.reviveProcessModelHistory(modelHistory, SecurityUtils.getCurrentUser(), action.getComment());
        } else {
            throw new BadRequestException("Invalid action to execute on model history " + modelHistoryId + ": " + action.getAction());
        }
    }
}
