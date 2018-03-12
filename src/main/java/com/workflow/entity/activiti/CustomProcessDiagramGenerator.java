package com.workflow.entity.activiti;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;

import java.awt.image.BufferedImage;
import java.util.List;

public class CustomProcessDiagramGenerator extends DefaultProcessDiagramGenerator {
    protected String activityFontName = "宋体";
    protected String labelFontName = "宋体";
    protected String annotationFontName = "宋体";

    public CustomProcessDiagramGenerator(final double scaleFactor){
        super(scaleFactor);
    }

    public CustomProcessDiagramGenerator(){
        super();
    }

    @Override
    public BufferedImage generateImage(BpmnModel bpmnModel, String imageType,
                                       List<String> highLightedActivities, List<String> highLightedFlows, double scaleFactor) {

        return generateImage(bpmnModel, imageType, highLightedActivities, highLightedFlows, this.activityFontName, this.labelFontName, this.annotationFontName, null, scaleFactor);
    }

}
