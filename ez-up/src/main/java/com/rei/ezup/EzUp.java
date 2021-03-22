package com.rei.ezup;

import static com.rei.ezup.TemplateConfig.CONFIG_GROOVY;
import static com.rei.ezup.TemplateConfig.DEFAULT_TEMPLATE;
import static io.methvin.watcher.DirectoryChangeEvent.EventType.DELETE;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import groovy.lang.GroovyRuntimeException;
import groovy.text.SimpleTemplateEngine;
import io.methvin.watcher.DirectoryChangeEvent;
import io.methvin.watcher.DirectoryWatcher;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

import com.rei.aether.Aether;
import com.rei.ezup.util.AntPathMatcher;
import com.rei.ezup.util.FileUtils;
import com.rei.ezup.util.GroovyScriptUtils;
import com.rei.ezup.util.PomUtils;

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
        return !filename.equals(CONFIG_GROOVY) &&
               !filename.equals(TemplateConfig.POSTINSTALL_GROOVY) &&
               !filename.endsWith(".class") &&
               !filename.endsWith(".retain");
    };
    
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    public EzUp(EzUpConfig globalConfig) {
        this.globalConfig = globalConfig;
    }
    
    public String generate(String templateGav, Path projectDir) throws IOException, ArtifactResolutionException {
        return generate(templateGav, null, projectDir);
    }
    
    public String generate(String templateGav, String subtemplate, Path projectDir) throws IOException, ArtifactResolutionException {
        return generate(getAether().resolveSingleArtifact(templateGav), subtemplate, projectDir);
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
                return null;
            }
            
            Path readme = projectDir.resolve("README.md");
            if (Files.exists(readme)) {
                return new String(Files.readAllBytes(readme));
            }
            return null;
        }
    }

    public CompletableFuture<Void> generateAndWatch(Path dir, Path projectDir) throws IOException, XmlPullParserException {
        Path pomFile = dir.resolve("pom.xml");

        while (!Files.exists(pomFile)) {
            if (pomFile.getParent() == null) {
                throw new IllegalArgumentException("no pom.xml found in: " + dir);
            }
            pomFile = pomFile.getParent().resolve("../pom.xml");
        }

        List<Path> templateConfigs = Files.walk(dir, 6)
                                    .filter(this::isMainTemplateConfig)
                                    .collect(toList());

        if (templateConfigs.isEmpty()) {
            throw new IllegalArgumentException("no template found under " + dir.toAbsolutePath());
        }

        if (templateConfigs.size() > 1) {
            templateConfigs = templateConfigs.stream().filter(f -> !f.toString().contains("target")).collect(toList());
            if (templateConfigs.size() > 1) {
                throw new IllegalArgumentException(
                        "multiple templates found under " + dir.toAbsolutePath() + ": " + templateConfigs);
            }
        }

        Path templateDir = templateConfigs.get(0).getParent().getParent();

        Artifact pomArtifact = PomUtils.readPomToArtifact(pomFile);
        List<URL> classpath = resolveClasspath(pomArtifact);

        try (TemplateArchive archive = new TemplateArchive(templateDir, pomArtifact, classpath)) {
            TemplateConfig config = TemplateConfig.load(archive, null, globalConfig, projectDir);

            List<Predicate<Path>> processFilters = getProcessFilters(config);
            List<Predicate<Path>> copyFilters = getCopyFilters(config);
            Function<Path, Path> renameTransformer = getRenameTransformer(config);
            Function<String, String> templateProcessor = getTemplateProcessor(archive, config, classpath);

            archive.unpackTo(config.getBasePath(), projectDir, copyFilters,
                             processFilters,
                             renameTransformer,
                             templateProcessor,
                             globalConfig.getProgressReporter());

            runPostInstallScript(projectDir, config.getBasePath(), archive, config);

            DirectoryWatcher watcher =
                    DirectoryWatcher.builder()
                                    .path(templateDir)
                                    .listener(e -> {
                                        Path mainTemplateDir = templateDir.resolve(DEFAULT_TEMPLATE);
                                        String relativePath = mainTemplateDir.relativize(e.path()).toString();

                                        if (e.eventType() == DELETE) {
                                            Path targetFile = renameTransformer.apply(projectDir.resolve(relativePath));
                                            Files.delete(targetFile);
                                            globalConfig.getProgressReporter().reportProgress(logger, "deleting {}", targetFile);
                                        } else {
                                            archive.unpackFileTo(config.getBasePath(),
                                                                 relativePath,
                                                                 projectDir,
                                                                 copyFilters,
                                                                 processFilters,
                                                                 renameTransformer,
                                                                 templateProcessor,
                                                                 globalConfig.getProgressReporter());
                                        }

                                    }).build();

            globalConfig.getProgressReporter().reportProgress("registered watch service for {}", templateDir);
            return watcher.watchAsync();
        }
    }

    private boolean isMainTemplateConfig(Path p) {
        return p.getParent() != null && p.getParent().getFileName().toString().equals(DEFAULT_TEMPLATE)
                       && p.getFileName().toString().equals(CONFIG_GROOVY);
    }

    public TemplateInfo getTemplateInfo(String templateGav) {
        return getTemplateInfo(getAether().resolveSingleArtifact(templateGav));
    }

    public TemplateInfo getTemplateInfo(Artifact artifact) {
        return FileUtils.withTempDir(tmpDir -> {
            try (TemplateArchive archive = new TemplateArchive(artifact, resolveClasspath(artifact))) {
                EzUpConfig infoConfig = new EzUpConfig(false, false, emptyMap());
                TemplateConfig config = TemplateConfig.load(archive, null, infoConfig, tmpDir);
                return config.getTemplateInfo();
            } catch (IOException e) {
                logger.warn("error loading template config!", e);
                return null;
            }
        });
    }

    List<Predicate<Path>> getCopyFilters(TemplateConfig config) {
        return List.of(NEVER_COPY, anyMatch(config.getIncludedFiles()), anyMatch(config.getExcludedFiles()).negate());
    }
    
    List<Predicate<Path>> getProcessFilters(TemplateConfig config) {
        return List.of(anyMatch(config.getProcessedFiles()), anyMatch(config.getUnprocessedFiles()).negate());
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
