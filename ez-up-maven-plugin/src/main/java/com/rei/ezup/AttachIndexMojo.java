package com.rei.ezup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import com.rei.ezup.index.TemplateIndex;
import com.rei.ezup.util.ZipUtils;

@Mojo(name="attach-index", defaultPhase= LifecyclePhase.PACKAGE)
public class AttachIndexMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true )
    MavenProject project;

    @Component
    MavenProjectHelper projectHelper;

    @Parameter(defaultValue = "${project.build.directory}/" + TemplateIndex.CLASSIFIER, readonly = true)
    File workingDir;

    @Parameter(defaultValue = "${project.build.directory}/"+ TemplateIndex.CLASSIFIER + ".jar", readonly = true)
    File destZip;

    @Parameter
    List<String> templates;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (templates == null || templates.isEmpty()) {
            templates = Collections.singletonList(project.getGroupId() + ":" + project.getArtifactId());
        }

        try {
            File indexFile = new File(workingDir, TemplateIndex.INDEX_PATH);
            indexFile.getParentFile().mkdirs();
            Files.write(indexFile.toPath(), String.join("\n", templates).getBytes());
            destZip.delete();
            ZipUtils.create(workingDir.toPath(), destZip.toPath());
            projectHelper.attachArtifact(project, destZip, TemplateIndex.CLASSIFIER);
        } catch (IOException e) {
            throw new MojoFailureException("error creating template index", e);
        }

    }
}
