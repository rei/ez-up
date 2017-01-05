package com.rei.ezup.util;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

public class NamingUtils {
    public static String toCamelCase(String input) {
        return Splitter.on(CharMatcher.JAVA_LETTER_OR_DIGIT.negate())
                       .splitToList(input).stream()
                       .map(s -> WordUtils.capitalize(s))
                       .collect(Collectors.joining());
    }
    
    public static String toHyphenated(String input) {
        return toLowerDelimited(input, "-");
    }
    
    public static String toLowerDelimited(String input, String delimiter) {
        return Stream.of(StringUtils.splitByCharacterTypeCamelCase(input))
                       .filter(s -> CharMatcher.JAVA_LETTER_OR_DIGIT.matchesAllOf(s))
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
