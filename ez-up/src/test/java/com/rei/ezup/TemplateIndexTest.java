package com.rei.ezup;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.HashMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.rei.aether.Aether;
import com.rei.ezup.index.TemplateIndex;

public class TemplateIndexTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void canIndexLocalDirectory() throws Exception {
        EzUp ezUp = new EzUp(new EzUpConfig(false, false, new HashMap<>()));
        Aether aether = Aether.fromMavenSettings();

        TemplateIndex idx = new TemplateIndex(ezUp, aether);
        idx.reindex();
        idx.getIndexedTemplates().forEach(t -> System.out.printf("%s - %s [%s]\n", t.getName(),
                                                                                   t.getDescription(),
                                                                                   t.getParameters().stream()
                                                                                    .map(ParameterInfo::getName)
                                                                                    .collect(joining(","))));

        assertTrue(idx.getIndexedTemplates().stream().anyMatch(ti -> ti.getName().equals("EZ-Up Template")));

        Path stored = tmp.newFile().toPath();
        idx.store(stored);

        idx = new TemplateIndex(ezUp, aether);
        idx.load(stored);
        assertTrue(idx.getIndexedTemplates().stream().anyMatch(ti -> ti.getName().equals("EZ-Up Template")));
    }

}