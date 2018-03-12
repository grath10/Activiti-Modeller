package com.workflow.entity.activiti;

import java.util.ArrayList;
import java.util.List;

public class ReviveModelResultRepresentation {
    private List<UnresolveModelRepresentation> unresolvedModels = new ArrayList<>();

    public List<UnresolveModelRepresentation> getUnresolvedModels() {
        return unresolvedModels;
    }

    public void setUnresolvedModels(List<ReviveModelResultRepresentation.UnresolveModelRepresentation> unresolvedModels) {
        this.unresolvedModels = unresolvedModels;
    }

    public static class UnresolveModelRepresentation {

        private String id;
        private String name;
        private String createdBy;

        public UnresolveModelRepresentation(String id, String name, String createdBy) {
            this.id = id;
            this.name = name;
            this.createdBy = createdBy;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

    }
}
