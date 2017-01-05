package com.rei.ezup;

public class ParameterInfo {
    private String name;
    private String description;
    private Object defaultValue;
    
    public ParameterInfo(String name, String description, Object defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
