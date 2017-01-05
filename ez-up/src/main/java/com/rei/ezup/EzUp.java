package com.rei.ezup;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import groovy.lang.GroovyRuntimeException;
import groovy.text.SimpleTemplateEngine;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

import com.rei.aether.Aether;
import com.rei.ezup.util.AntPathMatcher;
import com.rei.ezup.util.FileUtils;
import com.rei.ezup.util.GroovyScriptUtils;

public class EzUp {
    

    private static final String TEMPLATE_ERROR_MESSAGE = "error processing template!";

    private static final Logger logger = LoggerFactory.getLogger(EzUp.class);
    
    private EzUpConfig globalConfig;
    private static final Aether DEFAULT_AETHER = Aether.fromMavenSettings();

    
    private static final Predicate<Path> NEVER_COPY = p -> {
        if (p.getFileName() == null) {
            return true;
        }
        String filename = p.getFileName().toString();
        return !filename.equals(TemplateConfig.CONFIG_GROOVY) && 
               !filename.equals(TemplateConfig.POSTINSTALL_GROOVY) &&
               !filename.endsWith(".class") &&
               !filename.endsWith(".retain");
    };
    
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    public EzUp(EzUpConfig globalConfig) {
        this.globalConfig = globalConfig;
    }
    
    public String generate(String gavSpec, Path projectDir) throws IOException, ArtifactResolutionException {
        return generate(gavSpec, null, projectDir);
    }
    
    public String generate(String gavSpec, String subtemplate, Path projectDir) throws IOException, ArtifactResolutionException {
        return generate(getAether().resolveSingleArtifact(gavSpec), subtemplate, projectDir);
    }
    
    public String generate(Artifact templateArtifact, Path projectDir) throws IOException {
        return generate(templateArtifact, null, projectDir);
    }
    
    public String generate(Artifact templateArtifact, String subtemplate, Path projectDir) throws IOException {
        List<URL> classpath = resolveClasspath(templateArtifact);
        
        try (TemplateArchive archive = new TemplateArchive(templateArtifact, classpath)) {
            TemplateConfig config = TemplateConfig.load(archive, subtemplate, globalConfig, projectDir);
            
            archive.unpackTo(config.getBasePath(), projectDir, getCopyFilters(config), 
                    getProcessFilters(config), 
                    getRenameTransformer(config), 
                    getTemplateProcessor(archive, config, classpath),
                    globalConfig.getProgressReporter());
            
            runPostInstallScript(projectDir, config.getBasePath(), archive, config);
            
            if (subtemplate != null) {
                return "Successfully generated subtemplate " + subtemplate + "!";
            }
            
            Path readme = projectDir.resolve("README.md");
            if (Files.exists(readme)) {
                return new String(Files.readAllBytes(readme));
            }
            return "No readme assoicated with project!";
        }
    }

    public TemplateInfo getTemplateInfo(String templateGav) {
        return getTemplateInfo(getAether().resolveSingleArtifact(templateGav));
    }

    public TemplateInfo getTemplateInfo(Artifact artifact) {
        return FileUtils.withTempDir(tmpDir -> {
            try (TemplateArchive archive = new TemplateArchive(artifact, resolveClasspath(artifact))) {
                TemplateConfig config = TemplateConfig.load(archive, null, globalConfig, tmpDir);
                return config.getTemplateInfo();
            } catch (IOException e) {
                logger.warn("error loading template config!", e);
                return null;
            }
        });
    }

    List<Predicate<Path>> getCopyFilters(TemplateConfig config) {
        return Arrays.asList(NEVER_COPY, anyMatch(config.getIncludedFiles()), anyMatch(config.getExcludedFiles()).negate());
    }
    
    List<Predicate<Path>> getProcessFilters(TemplateConfig config) {
        return Arrays.asList(anyMatch(config.getProcessedFiles()), anyMatch(config.getUnprocessedFiles()).negate());
    }

    Function<Path, Path> getRenameTransformer(TemplateConfig config) {
        return in -> {
            String[] path = new String[] {in.toString()};
            config.getParameterValues().forEach((n, v) -> path[0] = path[0].replace("__" + n + "__", v.toString()));
            return Paths.get(path[0]);
        };
    }
    
    Function<String, String> getTemplateProcessor(TemplateArchive archive, TemplateConfig config, List<URL> classpath) {
        SimpleTemplateEngine templateEngine = createTemplateEngine(archive, config, classpath);
        return input -> {
            try {
                return templateEngine.createTemplate(input)
                                      .make(config.getParameterValues())
                                      .writeTo(new StringWriter())
                                      .toString();
            } catch (ClassNotFoundException | IOException | GroovyRuntimeException e) {
                throw new TemplatingException(TEMPLATE_ERROR_MESSAGE, e);
            }
        };
    }
    
    private void runPostInstallScript(Path projectDir, String root, TemplateArchive archive, TemplateConfig config) throws IOException {
        archive.read(root + "/" + TemplateConfig.POSTINSTALL_GROOVY).ifPresent(scriptText -> {
            try {
                globalConfig.getProgressReporter().reportProgress(logger, "running {}", TemplateConfig.POSTINSTALL_GROOVY);
                GroovyScriptUtils.runScript(config, 
                                            GroovyScriptUtils.getBinding(archive, globalConfig, projectDir),
                                            archive.getClasspath(),
                                            scriptText);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Predicate<Path> anyMatch(List<String> patterns) {
        return path -> patterns.stream().anyMatch(pattern -> {
            String matchPath = toMatchPath(path);
            boolean matched = PATH_MATCHER.match(pattern, matchPath);
            logger.debug("pattern {} {} {}", pattern, matched ? "matched" : "did not match", matchPath);
            return matched;
        });
    }

    private String toMatchPath(Path path) {
        String sanitized = path.toString().replace('\\', '/');
        if (sanitized.startsWith("/")) {
            return sanitized.substring(1);
        }
        return sanitized;
    }

    private List<URL> resolveClasspath(Artifact artifact) {
        if (!globalConfig.isResolveDependencies()) {
            return Collections.emptyList();
        }
        
        return getAether().resolveDependencies(artifact).stream().map(a -> {
            try {
                return a.getFile().toURI().toURL();
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        }).collect(toList());
    }
    
    private Aether getAether() {
        return MoreObjects.firstNonNull(globalConfig.getAether(), DEFAULT_AETHER);
    }
    
    private static SimpleTemplateEngine createTemplateEngine(TemplateArchive archive, TemplateConfig templateConfig, List<URL> classpath) {
        return new SimpleTemplateEngine(GroovyScriptUtils.getGroovyShell(
                                            GroovyScriptUtils.getBinding(archive, templateConfig.getGlobalConfig(), null), 
                                            classpath));
    }

}
