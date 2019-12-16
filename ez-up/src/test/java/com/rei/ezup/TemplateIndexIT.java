package com.rei.ezup;

import com.rei.aether.Aether;
import com.rei.ezup.index.TemplateIndex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateIndexIT {
    @Test
    void canIndexLocalDirectory(@TempDir Path tmp) throws Exception {
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

        Path stored = Files.createTempFile(tmp, "idx", "");
        idx.store(stored);

        idx = new TemplateIndex(ezUp, aether);
        idx.load(stored);
        assertTrue(idx.getIndexedTemplates().stream().anyMatch(ti -> ti.getName().equals("EZ-Up Template")));
    }

}