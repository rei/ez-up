package com.rei.ezup.index;

import static com.rei.ezup.index.TemplateIndex.EXTENSION;
import static java.util.Collections.emptyList;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.aether.repository.RemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nexus2RemoteRepositoryIndexer implements RemoteRepositoryIndexer {

    private static final Logger logger = LoggerFactory.getLogger(Nexus2RemoteRepositoryIndexer.class);

    /*
    nexus2 only allows searching based on packaging if group id, artifact id, or version are specified, so we search these common
    starts to group ids
     */
    private static final String[] GROUP_ID_PREFIXES = new String[] {
            "c" /* co, ch, com */,
            "d" /* de */,
            "e" /* edu */,
            "i" /* io */ ,
            "o" /* org */,
            "n"  /* net, nl, nz */,
            "u" /* uk */
    };

    public static final String STATUS_PATH = "/service/local/status";
    public static final String SEARCH_PATH = "/service/local/data_index?p=" + EXTENSION + "g=";
    public static final String NEXUS_APP_NAME = "Nexus Repository Manager";
    public static final String VERSION_2 = "<version>2.";
    private HttpClient httpClient = HttpClientBuilder.create().setMaxConnTotal(20).setMaxConnPerRoute(10).build();

    @Override
    public List<String> getIndexes(RemoteRepository repo) throws IOException {
        if (isNexus2(repo)) {
            logger.info("indexing nexus repository {}", repo.getHost());
            List<String> results = new ArrayList<>();

            for (String groupIdPrefix : GROUP_ID_PREFIXES) {

                HttpResponse response = httpClient.execute(new HttpGet(uri(repo, SEARCH_PATH + groupIdPrefix)));

                if (response.getStatusLine().getStatusCode() > 299) {
                    logger.debug("nexus returned unexpected status: {}", response.getStatusLine());
                    continue;
                }

                Nexus2SearchResults searchResults = JAXB.unmarshal(response.getEntity().getContent(), Nexus2SearchResults.class);
                searchResults.data.stream()
                                  .filter(r -> r.groupId != null && r.artifactId != null)
                                  .map(r -> r.groupId + ":" + r.artifactId)
                                  .forEach(results::add);
            }

            return results;
        }

        return emptyList();
    }

    private boolean isNexus2(RemoteRepository repo) throws IOException {
        HttpResponse response = httpClient.execute(new HttpGet(uri(repo, STATUS_PATH)));
        if (response.getStatusLine().getStatusCode() != 200) {
            return false;
        }
        String responseBody = EntityUtils.toString(response.getEntity());
        return responseBody.contains(NEXUS_APP_NAME) && responseBody.contains(VERSION_2);
    }

    private URI uri(RemoteRepository repo, String path) throws IOException {
        return URI.create(repo.getProtocol()+ "://" + repo.getHost() + path);
    }

    public static class Nexus2SearchResults {
        private List<Nexus2SearchResult> data;

        public List<Nexus2SearchResult> getData() {
            return data;
        }

        public void setData(List<Nexus2SearchResult> data) {
            this.data = data;
        }
    }

    public static class Nexus2SearchResult {
        private String groupId;
        private String artifactId;
        private String version;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
