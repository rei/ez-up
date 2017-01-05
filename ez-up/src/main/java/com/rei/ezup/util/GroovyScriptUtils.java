package com.rei.ezup.util;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import com.rei.ezup.EzUpConfig;
import com.rei.ezup.EzUpScript;
import com.rei.ezup.TemplateArchive;
import com.rei.ezup.TemplateConfig;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class GroovyScriptUtils {
    public static void runScript(TemplateConfig config, Binding binding, List<URL> classpath, String scriptText) throws IOException {
        GroovyShell shell = getGroovyShell(binding, classpath);
        EzUpScript script = (EzUpScript) shell.parse(scriptText);
        script.setConfig(config);
        script.run();
    }

    public static GroovyShell getGroovyShell(Binding binding, List<URL> classpath) {
        GroovyShell shell = new GroovyShell(GroovyScriptUtils.class.getClassLoader(), binding, getCompilerConfig(classpath));
        return shell;
    }
    
    public static CompilerConfiguration getCompilerConfig(List<URL> classpath) {
        CompilerConfiguration compilerConfig = new CompilerConfiguration();
        if (!classpath.isEmpty()) {
            compilerConfig.setClasspathList(classpath.stream().map(URL::toString).collect(toList()));
        }
        
        compilerConfig.addCompilationCustomizers(new ImportCustomizer().addStaticStars(NamingUtils.class.getName()));
        compilerConfig.setScriptBaseClass(EzUpScript.class.getName());
        return compilerConfig;
    }

    public static Binding getBinding(TemplateArchive archive, EzUpConfig globalConfig, Path projectDir) {
        Binding binding = new Binding();
        binding.setProperty("globalConfig", globalConfig);
        
        if (projectDir != null) {
            binding.setProperty("projectDir", projectDir.toFile());
        }
        
        binding.setProperty("templateVersion", archive.getVersion());
        binding.setProperty("templateArtifactId", archive.getArtifactId());
        binding.setProperty("templateGroupId", archive.getGroupId());
        binding.setProperty("templateClassifier", archive.getClassifier());
        return binding;
    }
}
