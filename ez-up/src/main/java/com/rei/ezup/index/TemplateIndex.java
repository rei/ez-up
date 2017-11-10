package com.rei.ezup.index;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import groovy.json.JsonSlurper;

import org.eclipse.aether.repository.RemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rei.aether.Aether;
import com.rei.ezup.EzUp;
import com.rei.ezup.TemplateInfo;
import com.rei.ezup.util.ZipUtils;

public class TemplateIndex {
    private static final Logger logger = LoggerFactory.getLogger(TemplateIndex.class);

    public static final String CLASSIFIER = "ezup-idx";
    public static final String INDEX_PATH = "/META-INF/ezup.index";
    private static final String RELEASE_VERSION = ":RELEASE";

    private static final String CENTRAL_SEARCH_URL =
            "https://search.maven.org/solrsearch/select?q=l:" + CLASSIFIER + "&core=gav&rows=100&wt=json";

    private static final List<RemoteRepositoryIndexer> REMOTE_REPOSITORY_INDEXERS = Arrays.asList(
            new Nexus2RemoteRepositoryIndexer()
    );

    private EzUp ezup;
    private Aether aether;
    private Map<String, TemplateInfo> templates = new ConcurrentHashMap<>();

    public TemplateIndex(EzUp ezup, Aether aether) {
        this.ezup = ezup;
        this.aether = aether;
    }

    public void store(Path file) throws IOException {
        Files.write(file, templates.keySet());
    }

    public void load(Path file) throws IOException {
        Files.readAllLines(file).forEach(this::index);
    }

    public void reindex() {
        logger.info("performing reindex of known templates...");
        Stream.<Runnable>of(
                () -> reindexDirectory(aether.getLocalRepository().getBasedir().toPath()),
                () -> aether.getConfiguredRepositories().parallelStream().forEach(this::index),
                this::indexMavenCentral
        ).parallel().forEach(Runnable::run);
    }

    @SuppressWarnings("unchecked")
    private List<String> indexMavenCentral() {
        try {
            logger.info("indexing maven central...");
            Map<String, Map<String, Object>> result = (Map<String, Map<String, Object>>) new JsonSlurper().parse(new URL(CENTRAL_SEARCH_URL));
            List<Map<String, Object>> resultRecords = (List<Map<String, Object>>) result.get("response").get("docs");
            return resultRecords.stream()
                                .map(r -> r.get("g") + ":" + r.get("a"))
                                .collect(toList());
        } catch (MalformedURLException e) {
            return Collections.emptyList();
        }
    }

    private void index(RemoteRepository remoteRepository) {
        REMOTE_REPOSITORY_INDEXERS.stream().flatMap(ri -> {
            try {
                return ri.getIndexes(remoteRepository).stream();
            } catch (IOException e) {
                return Stream.empty();
            }
        })
          .distinct()
          .forEach(this::index);
    }

    private void reindexDirectory(Path path) {
        try {
            logger.info("re-indexing directory: {}", path);
            Files.walk(path)
                 .parallel()
                 .filter(p -> p.toString().endsWith("-" + CLASSIFIER + ".jar"))
                 .forEach(this::indexJar);
        } catch (IOException e) {
            e.printStackTrace(); // TODO
        }
    }

    private void indexJar(Path jar) {
        logger.debug("indexing jar file: {}", jar);
        try (FileSystem jarFs = ZipUtils.createZipFileSystem(jar, false)) {
            Path path = jarFs.getPath(INDEX_PATH);
            Files.readAllLines(path).forEach(this::index);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void index(String partialCoords) {
        templates.put(partialCoords, ezup.getTemplateInfo(partialCoords + RELEASE_VERSION));
    }

    public List<TemplateInfo> getIndexedTemplates() {
        return new ArrayList<>(templates.values());
    }
}
