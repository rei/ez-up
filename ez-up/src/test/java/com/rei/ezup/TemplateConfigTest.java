package com.rei.ezup;

import com.google.common.collect.ImmutableMap;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TemplateConfigTest extends BaseTemplateTest {
    @Test
    public void testLoad(@TempDir Path tmp) throws Exception {
        try(TemplateArchive archive = getTestTemplate(tmp)) {
            EzUpConfig globalConfig = new EzUpConfig(false, false, ImmutableMap.of("global", "true", "includeFoo", "true"));

            TemplateConfig config = TemplateConfig.load(archive, null, globalConfig, tmp);
            Map<String, Object> params = config.getParameterValues();
            System.out.println(params);
            assertEquals(11, params.size());
            assertEquals(1, config.getIncludedFiles().size());
            assertEquals(1, config.getExcludedFiles().size());
            assertNotNull(params.get("AppName"));

            TemplateInfo templateInfo = config.getTemplateInfo();
            System.out.println(templateInfo.getName() + " - " + templateInfo.getDescription());
            assertEquals("Chairlift Test Template", templateInfo.getName());
            assertEquals("This is a test", templateInfo.getDescription());
        }
    }

}
