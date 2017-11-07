package com.rei.ezup.index;

import static com.rei.ezup.index.TemplateIndex.CLASSIFIER;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import groovy.json.JsonSlurper;

import org.eclipse.aether.repository.RemoteRepository;

public class MavenCentralRemoteRepositoryIndexer implements RemoteRepositoryIndexer {
    public static final String SEARCH_URL =
            "https://search.maven.org/solrsearch/select?q=l:" + CLASSIFIER + "&core=gav&rows=100&wt=json";

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getIndexes(RemoteRepository repo) throws IOException {
        Map<String, Map<String, Object>> result = (Map<String, Map<String, Object>>) new JsonSlurper().parse(new URL(SEARCH_URL));
        List<Map<String, Object>> resultRecords = (List<Map<String, Object>>) result.get("response").get("docs");
        return resultRecords.stream()
                            .map(r -> r.get("g") + ":" + r.get("a"))
                            .collect(toList());
    }
}
