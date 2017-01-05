package com.rei.ezup;

import java.util.HashMap;
import java.util.Map;

import com.rei.aether.Aether;

public class EzUpConfig {
    private boolean interactive;
    private Map<String, String> suppliedParameters = new HashMap<>();
    private boolean resolveDependencies;
    private Aether aether;
    private ProgressReporter progress;
    
    public EzUpConfig(boolean interactive, boolean resolveDependencies, Map<String, String> suppliedParameters) {
        this(interactive, resolveDependencies, suppliedParameters, null, new ProgressReporter.Noop());
    }
    
    public EzUpConfig(boolean interactive, boolean resolveDependencies, Map<String, String> suppliedParameters, Aether aether, ProgressReporter progress) {
        this.interactive = interactive;
        this.resolveDependencies = resolveDependencies;
        this.suppliedParameters = suppliedParameters;
        this.aether = aether;
        this.progress = progress;
    }
    
    public Aether getAether() {
        return aether;
    }
    
    public void setAether(Aether aether) {
        this.aether = aether;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    public Map<String, String> getSuppliedParameters() {
        return suppliedParameters;
    }

    public void setSuppliedParameters(Map<String, String> suppliedParameters) {
        this.suppliedParameters = suppliedParameters;
    }

    public boolean isResolveDependencies() {
        return resolveDependencies;
    }

    public void setResolveDependencies(boolean resolveDependencies) {
        this.resolveDependencies = resolveDependencies;
    }
    
    public ProgressReporter getProgressReporter() {
        return progress;
    }
    
    public void setProgressReporter(ProgressReporter progress) {
        this.progress = progress;
    }
}