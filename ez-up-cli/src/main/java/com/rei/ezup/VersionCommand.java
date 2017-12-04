package com.rei.ezup;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;

import com.rei.ezup.EzUpCli.MainArgs;

public class VersionCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(VersionCommand.class);

    @Override
    public void execute(JCommander jc, MainArgs mainArgs) throws Throwable {
        InputStream buildInfo = getClass().getClassLoader().getResourceAsStream("META-INF/build-info.properties");
        if (buildInfo == null) {
            logger.info("no version info available!");
        }

        Properties props = new Properties();
        props.load(buildInfo);

        logger.info("EZ-UP CLI");
        logger.info("Version {} - Built On {}", props.getProperty("build.version"), props.getProperty("build.time"));
    }
}
