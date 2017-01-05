package com.rei.ezup;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import com.rei.ezup.util.ZipUtils;

public class BaseTemplateTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    public TemplateArchive getTestTemplate() throws Exception {
        Artifact artifact = getTestTemplateArtifact();
        TemplateArchive archive = new TemplateArchive(artifact);
        archive.init();
        return archive;
    }

    public Artifact getTestTemplateArtifact() throws Exception {
        String name = "chairlift-test";
        Artifact artifact = new DefaultArtifact("com.rei.test", name, "zip", "1");
        Path templateZip = tmp.getRoot().toPath().resolve(name + ".zip");
        createTemplateZip(name, templateZip);
        artifact = artifact.setFile(templateZip.toFile());
        return artifact;
    }
    
    public static void createTemplateZip(String testTemplateName, Path dest) throws Exception {
        URL url = BaseTemplateTest.class.getClassLoader().getResource("template/config.groovy");
        Path folder = Paths.get(url.toURI().resolve(".."));
        ZipUtils.create(folder, dest);
    }
}
