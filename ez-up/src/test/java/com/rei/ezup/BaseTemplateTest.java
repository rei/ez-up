package com.rei.ezup;

import com.rei.ezup.util.ZipUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BaseTemplateTest {
    public TemplateArchive getTestTemplate(Path tmp) throws Exception {
        Artifact artifact = getTestTemplateArtifact(tmp);
        TemplateArchive archive = new TemplateArchive(artifact);
        return archive;
    }

    public Artifact getTestTemplateArtifact(Path tmp) throws Exception {
        String name = "chairlift-test";
        Artifact artifact = new DefaultArtifact("com.rei.test", name, "zip", "1");
        Path templateZip = tmp.resolve(name + ".zip");
        createTemplateZip(name, templateZip);
        artifact = artifact.setFile(templateZip.toFile());
        return artifact;
    }
    
    public static void createTemplateZip(String testTemplateName, Path dest) throws Exception {
        Path folder = getTemplateRootDir();
        ZipUtils.create(folder, dest);
    }

    public static Path getTemplateRootDir() throws URISyntaxException {
        URL url = BaseTemplateTest.class.getClassLoader().getResource("template/config.groovy");
        Path folder = Paths.get(url.toURI().resolve(".."));
        return folder;
    }
}
