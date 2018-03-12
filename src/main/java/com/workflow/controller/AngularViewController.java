package com.workflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AngularViewController {
    @RequestMapping("/display/displaymodel")
    public String getDisplayModel() {
        return "views/displaymodel";
    }

    @RequestMapping("/**/views/process")
    public String getProcessPage() {
        return "views/process";
    }

    @RequestMapping("/**/views/processes")
    public String getProcessesPage() {
        return "views/processes";
    }

    @RequestMapping("/**/views/form")
    public String getFormPage() {
        return "views/form";
    }

    @RequestMapping("/**/views/forms")
    public String getFormsPage() {
        return "views/forms";
    }

    @RequestMapping("/**/views/editor")
    public String getEditorPage() {
        return "views/editor";
    }

    @RequestMapping("/**/views/popup/process-import")
    public String getImportPage() {
        return "views/popup/process-import";
    }

    @RequestMapping("/**/views/popup/process-create")
    public String getCreatePage() {
        return "views/popup/process-create";
    }

    @RequestMapping("/**/views/form-builder")
    public String getFormBuilder() {
        return "views/form-builder";
    }

    @RequestMapping("/views/popover/history")
    public String getHistory() {
        return "views/popover/history";
    }

    @RequestMapping("/views/popup/model-edit")
    public String getEditView() {
        return "views/popup/model-edit";
    }

    @RequestMapping("/views/popup/process-duplicate")
    public String getDuplicateView() {
        return "views/popup/process-duplicate";
    }

    @RequestMapping("/views/popup/model-delete")
    public String getDeleteView() {
        return "views/popup/model-delete";
    }
}