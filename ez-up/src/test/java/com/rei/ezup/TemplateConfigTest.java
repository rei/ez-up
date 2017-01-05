package com.rei.ezup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class TemplateConfigTest extends BaseTemplateTest {
    @Test
    public void testLoad() throws Exception {
        TemplateArchive archive = getTestTemplate();
        EzUpConfig globalConfig = new EzUpConfig(false, false, ImmutableMap.of("global", "true", "includeFoo", "true"));
        
        TemplateConfig config = TemplateConfig.load(archive, null, globalConfig, tmp.getRoot().toPath());
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
