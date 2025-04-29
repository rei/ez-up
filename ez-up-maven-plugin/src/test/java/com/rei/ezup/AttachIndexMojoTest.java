package com.rei.ezup;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AttachIndexMojoTest {
    @TempDir
    public Path tmp;

    @Test
    public void execute() throws Exception {
        AttachMarkerMojo mojo = new AttachMarkerMojo();
        mojo.markerFile = new File(tmp.toFile(), "test.ezup");

        mojo.project = new MavenProject();
        mojo.project.setGroupId("group.id");
        mojo.project.setArtifactId("artifact.id");

        MockMavenProjectHelper mockProjectHelper = new MockMavenProjectHelper();
        mojo.projectHelper = mockProjectHelper;

        mojo.execute();
        assertTrue(mockProjectHelper.attached);
    }

    private static class MockMavenProjectHelper implements MavenProjectHelper {
        boolean attached = false;

        @Override
        public void attachArtifact(MavenProject project, File artifactFile, String artifactClassifier) {

        }

        @Override
        public void attachArtifact(MavenProject project, String artifactType, File artifactFile) {
            assertEquals("ezup", artifactType);
            try {
                assertEquals(project.getGroupId() + ":" + project.getArtifactId(),
                             new String(Files.readAllBytes(artifactFile.toPath())));

                attached = true;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
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
