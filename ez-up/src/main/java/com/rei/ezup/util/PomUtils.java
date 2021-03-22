package com.rei.ezup.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

public class PomUtils {
    public static Model readPom(Path pomFile) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        return reader.read(Files.newBufferedReader(pomFile));
    }

    public static Artifact readPomToArtifact(Path pomFile) throws IOException, XmlPullParserException {
        Model model = readPom(pomFile);
        return new DefaultArtifact(getGroupId(model), model.getArtifactId(), "pom", getVersion(model)).setFile(pomFile.toFile());
    }

    public static String getGroupId(Model model) {
        return model.getGroupId() != null ? model.getGroupId() : model.getParent().getGroupId();
    }

    public static String getVersion(Model model) {
        String version = model.getVersion() != null ? model.getVersion() : model.getParent().getVersion();
        for (Object name : System.getProperties().keySet()) {
            version = version.replace("${" + name + "}", System.getProperties().getProperty(name.toString()));
        }
        for (Object name : model.getProperties().keySet()) {
            version = version.replace("${" + name + "}", model.getProperties().getProperty(name.toString()));
        }
        return version;
    }
}
