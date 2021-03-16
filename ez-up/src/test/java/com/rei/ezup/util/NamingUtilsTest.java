package com.rei.ezup.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NamingUtilsTest {

    @Test
    public void testToCamelCase() {
        assertEquals("ThisIsATest", NamingUtils.toCamelCase("this-is-a-test"));
        assertEquals("ThisIsATest", NamingUtils.toCamelCase("this is a test"));
        assertEquals("SomeTest", NamingUtils.toCamelCase("someTest"));
        assertEquals("Blah", NamingUtils.toCamelCase("blah"));
    }

    @Test
    public void testToHyphenated() {
        assertEquals("this-is-a-test", NamingUtils.toHyphenated("ThisIsATest"));
        assertEquals("this-is-a-test", NamingUtils.toHyphenated("This is A test"));
        assertEquals("test", NamingUtils.toHyphenated("Test"));
        assertEquals("a-test", NamingUtils.toHyphenated("A-Test"));
    }

    @Test
    public void testToNatural() {
        assertEquals("This is a test", NamingUtils.toNatural("this-is-a-test"));
        assertEquals("This is a test", NamingUtils.toNatural("ThisIsATest"));
    }
    
    @Test
    public void testToTitleCase() {
        assertEquals("This Is A Test", NamingUtils.toTitleCase("this-is-a-test"));
        assertEquals("This Is A Test", NamingUtils.toTitleCase("ThisIsATest"));
    }

}
