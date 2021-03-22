package com.rei.ezup;

import java.util.List;

import org.eclipse.aether.artifact.Artifact;

public class TemplateInfo {
    private String gav;
    private String name;
    private String description;
    private List<ParameterInfo> parameters;
    private List<String> subtemplates;

    public String getGav() {
        return gav;
    }

    public void setGav(String gav) {
        this.gav = gav;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterInfo> parameters) {
        this.parameters = parameters;
    }

    public List<String> getSubtemplates() {
        return subtemplates;
    }

    public void setSubtemplates(List<String> subtemplates) {
        this.subtemplates = subtemplates;
    }
}
