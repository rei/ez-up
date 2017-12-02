package com.rei.ezup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

@Mojo(name="attach-marker", defaultPhase= LifecyclePhase.PACKAGE)
public class AttachMarkerMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true )
    MavenProject project;

    @Component
    MavenProjectHelper projectHelper;

    @Parameter(defaultValue = "${project.build.directory}/${project.artifactId}.ezup", readonly = true)
    File markerFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Files.write(markerFile.toPath(), (project.getGroupId() + ":" + project.getArtifactId()).getBytes());
            projectHelper.attachArtifact(project, "ezup", markerFile);
        } catch (IOException e) {
            throw new MojoFailureException("error creating template indexing marker", e);
        }

    }
}
