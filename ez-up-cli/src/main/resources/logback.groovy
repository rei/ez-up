import static ch.qos.logback.classic.Level.INFO

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.encoder.PatternLayoutEncoder

def logFormat = "%msg%n"

logger('com.rei.ezup', Level.toLevel(System.getProperty("logging.level")))

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) { pattern = logFormat }
}
root(INFO, ["STDOUT"])