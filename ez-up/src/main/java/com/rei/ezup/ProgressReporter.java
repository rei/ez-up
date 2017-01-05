package com.rei.ezup;

import org.slf4j.Logger;

public interface ProgressReporter {
    default void reportProgress(Logger logger, String message, Object... args) {
        logger.info(message, args);
        reportProgress(message, args);
    }
    
    void reportProgress(String message, Object... args);
    
    public static class Noop implements ProgressReporter {
        @Override
        public void reportProgress(String message, Object... args) {}
    }
}
