package com.rei.ezup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.rei.ezup.index.TemplateIndex;
import com.rei.ezup.util.ZipUtils;

public class AttachIndexMojoTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void execute() throws Exception {
        AttachIndexMojo mojo = new AttachIndexMojo();
        mojo.workingDir = tmp.newFolder();
        mojo.destZip = tmp.newFile("index.jar");

        mojo.project = new MavenProject();
        mojo.project.setGroupId("group.id");
        mojo.project.setArtifactId("artifact.id");

        MockMavenProjectHelper mockProjectHelper = new MockMavenProjectHelper();
        mojo.projectHelper = mockProjectHelper;
        mojo.templates = new ArrayList<>();

        mojo.execute();
        assertTrue(mockProjectHelper.attached);
    }

    private static class MockMavenProjectHelper implements MavenProjectHelper {
        boolean attached = false;

        @Override
        public void attachArtifact(MavenProject project, File artifactFile, String artifactClassifier) {
            assertEquals(TemplateIndex.CLASSIFIER, artifactClassifier);
            assertNotNull(artifactFile);
            try {
                Path path = ZipUtils.createZipFileSystem(artifactFile.toPath(), false).getPath(TemplateIndex.INDEX_PATH);
                List<String> lines = Files.readAllLines(path);
                System.out.println(lines);
                assertEquals(1, lines.size());
                assertEquals("group.id:artifact.id", lines.get(0));
                attached = true;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        }

        @Override
        public void attachArtifact(MavenProject project, String artifactType, File artifactFile) {

        }

        @Override
        public void attachArtifact(MavenProject project, String artifactType, String artifactClassifier, File artifactFile) {

        }

        @Override
        public void addResource(MavenProject project, String resourceDirectory, List<String> includes,
                                List<String> excludes) {

        }

        @Override
        public void addTestResource(MavenProject project, String resourceDirectory, List<String> includes,
                                    List<String> excludes) {

        }
    }
}