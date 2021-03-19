package com.rei.ezup;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.aether.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateArchive implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(TemplateArchive.class);

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;
    private final String extension;

    private FileSystem fileSystem;
    private Path basePath;
    private final List<URL> classpath;
    private final boolean closeable;

    public TemplateArchive(Artifact artifact) throws IOException {
        this(artifact, Collections.emptyList());
    }
    
    public TemplateArchive(Artifact artifact, List<URL> classpath) throws IOException {
        this.groupId = artifact.getGroupId();
        this.artifactId = artifact.getArtifactId();
        this.version = artifact.getVersion();
        this.classifier = artifact.getClassifier();
        this.extension = artifact.getExtension();
        this.classpath = classpath;
        final URI uri = URI.create("jar:file:" + artifact.getFile().toURI().getPath());
        fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
        basePath = Paths.get("/");
        this.closeable = true;
    }    

    public TemplateArchive(Path templateDir, Artifact artifact, List<URL> classpath) {
        this.groupId = artifact.getGroupId();
        this.artifactId = artifact.getArtifactId();
        this.version = artifact.getVersion();
        this.classifier = artifact.getClassifier();
        this.extension = artifact.getExtension();
        this.classpath = classpath;
        this.fileSystem = FileSystems.getDefault();
        this.basePath = templateDir;
        this.closeable = false;
    }

    public Optional<String> read(String path) throws IOException {
        Path p = fileSystem.getPath(basePath.toString(), path);
        if (Files.exists(p)) {
            return Optional.of(new String(Files.readAllBytes(p)));
        }
        
        return Optional.empty();
    }
    
    public boolean exists(String path) throws IOException {
        return Files.exists(fileSystem.getPath(basePath.toString(), path));
    }
    
    public List<String> list(String folder) throws IOException {
        Path path = fileSystem.getPath(basePath.toString(), folder);
        if (!Files.exists(path)) {
            return Collections.emptyList();
        }
        return Files.list(path)
                    .map(p -> p.getFileName().toString())
                    .collect(toList());
    }

    public String getVersion() {
        return version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getExtension() {
        return extension;
    }

    public String getGav() {
        StringBuilder buffer = new StringBuilder(128);
        buffer.append(getGroupId());
        buffer.append(':').append(getArtifactId());
        buffer.append(':').append(getExtension());
        if (getClassifier().length() > 0) {
            buffer.append(':').append(getClassifier());
        }
        buffer.append(':').append(getVersion());
        return buffer.toString();
    }

    public List<URL> getClasspath() {
        return classpath;
    }

    public void unpackFileTo(String base,
                             String file,
                             Path projectDir,
                             List<Predicate<Path>> copyFilters,
                             List<Predicate<Path>> processingFilters,
                             Function<Path, Path> filenameTransformer,
                             Function<String, String> contentTransformer,
                             ProgressReporter progressReporter) throws IOException {

        final Path root = fileSystem.getPath(basePath.toString(), base);
        Path filePath = root.resolve(file);
        if (Files.notExists(filePath) || !Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException("no file at " + file);
        }
        unpackFileTo(root, filePath, projectDir, copyFilters, processingFilters, filenameTransformer, contentTransformer,
                     progressReporter);
    }

    public void unpackTo(String base, Path projectDir,
                         List<Predicate<Path>> copyFilters,
                         List<Predicate<Path>> processingFilters,
                         Function<Path, Path> filenameTransformer,
                         Function<String, String> contentTransformer,
                         ProgressReporter progressReporter) throws IOException {

        // if the destination doesn't exist, create it
        if (Files.notExists(projectDir)) {
            Files.createDirectories(projectDir);
        }

        final Path root = fileSystem.getPath(basePath.toString(), base);

        // walk the zip file tree and copy files to the destination
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                unpackFileTo(root, file, projectDir, copyFilters, processingFilters,
                             filenameTransformer, contentTransformer, progressReporter);

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (allMatch(copyFilters, root.relativize(dir))) {
                    Path rawDest = Paths.get(projectDir.toString(), root.relativize(dir).toString());
                    Path dest = filenameTransformer.apply(rawDest);
                    progressReporter.reportProgress(logger, "creating directory {} in {}", projectDir.relativize(dest), projectDir);
                    Files.createDirectories(dest);
                }
                return FileVisitResult.CONTINUE;
            }


        });
    }

    private void unpackFileTo(Path root, Path file, Path projectDir,
                              List<Predicate<Path>> copyFilters,
                              List<Predicate<Path>> processingFilters,
                              Function<Path, Path> filenameTransformer,
                              Function<String, String> contentTransformer,
                              ProgressReporter progressReporter) throws IOException {

        if (!allMatch(copyFilters, root.relativize(file))) {
            return;
        }

        byte[] content = Files.readAllBytes(file);
        try {
            content = allMatch(processingFilters, root.relativize(file)) ? transform(contentTransformer, content) : content;
        } catch (TemplatingException e) {
            throw new RuntimeException("error parsing template " + file + ": " + e.getCause().getMessage(), e);
        }

        Path rawDest = projectDir.resolve(stringLeadingSlash(root.relativize(file).toString()));
        final Path dest = filenameTransformer.apply(rawDest);
        progressReporter.reportProgress(logger, "creating {} in {}", projectDir.relativize(dest), projectDir);
        Files.write(dest, content);
    }

    private byte[] transform(Function<String, String> contentTransformer, byte[] content) {
        return contentTransformer.apply(new String(content)).getBytes();
    }

    private boolean allMatch(List<Predicate<Path>> copyFilters, Path file) {
        return copyFilters.stream().allMatch(p -> p.test(file));
    }

    @Override
    public void close() {
        if (!closeable) {
            return;
        }
        try {
            fileSystem.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private static String stringLeadingSlash(String in) {
        return in.startsWith("/") || in.startsWith("\\") ? in.substring(1) : in;
    }

}
