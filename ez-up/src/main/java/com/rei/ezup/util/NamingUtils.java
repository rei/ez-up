package com.rei.ezup.util;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

@SuppressWarnings("deprecation")
public class NamingUtils {
    public static String toCamelCase(String input) {
        return Splitter.on(CharMatcher.javaLetterOrDigit().negate())
                       .splitToList(input).stream()
                       .map(WordUtils::capitalize)
                       .collect(Collectors.joining());
    }
    
    public static String toHyphenated(String input) {
        return toLowerDelimited(input, "-");
    }
    
    public static String toLowerDelimited(String input, String delimiter) {
        return Stream.of(StringUtils.splitByCharacterTypeCamelCase(input))
                       .filter(s -> CharMatcher.javaLetterOrDigit().matchesAllOf(s))
                       .map(String::toLowerCase)
                       .collect(Collectors.joining(delimiter));
    }
    
    public static String toNatural(String input) {
        return StringUtils.capitalize(toLowerDelimited(input, " "));
    }
    
    public static String toTitleCase(String input) {
        return WordUtils.capitalizeFully(toLowerDelimited(input, " "));
    }
}
