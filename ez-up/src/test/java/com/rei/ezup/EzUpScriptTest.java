package com.rei.ezup;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EzUpScriptTest {

    @Test
    void canGetParams() {
        EzUpScript script = new EzUpScript();
        Map<String, String> suppliedParams = Maps.newHashMap(ImmutableMap.of("bool", "true", "num", "123"));
        script.setConfig(new TemplateConfig(new EzUpConfig(false, false, suppliedParams)));
        assertEquals(true, script.param("bool", "", false));
        suppliedParams.put("bool", "false");
        assertEquals(false, script.param("bool", "", true));
        assertTrue(script.param("num", "", 456) instanceof Number);
    }

}
