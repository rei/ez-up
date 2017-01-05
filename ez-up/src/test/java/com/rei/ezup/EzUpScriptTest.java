package com.rei.ezup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.rei.ezup.EzUpConfig;
import com.rei.ezup.EzUpScript;
import com.rei.ezup.TemplateConfig;

public class EzUpScriptTest {

    @Test
    public void canGetParams() {
        EzUpScript script = new EzUpScript();
        Map<String, String> suppliedParams = Maps.newHashMap(ImmutableMap.of("bool", "true", "num", "123"));
        script.setConfig(new TemplateConfig(new EzUpConfig(false, false, suppliedParams)));
        assertEquals(true, script.param("bool", "", false));
        suppliedParams.put("bool", "false");
        assertEquals(false, script.param("bool", "", true));
        assertTrue(script.param("num", "", 456) instanceof Number);
    }

}
