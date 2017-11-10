package com.rei.ezup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rei.aether.Aether;
import com.rei.ezup.index.TemplateIndex;

public class EzUpContext {
    private static final Logger logger = LoggerFactory.getLogger(EzUpContext.class);

    private EzUp ezUp;
    private Aether aether;

    public static EzUpContext fromArgs(EzUpCli.MainArgs args, Map<String, String> params) {
        EzUpContext ezUpContext = new EzUpContext();
        ezUpContext.aether = Aether.fromMavenSettings();
        ezUpContext.ezUp = new EzUp(new EzUpConfig(!args.nonInteractive, true, params, ezUpContext.aether,
                                                   new ProgressReporter.Noop()));
        return ezUpContext;
    }

    public EzUp getEzUp() {
        return ezUp;
    }

    public TemplateIndex getTemplateIndex(boolean skipIndexing, boolean forceIndexing) throws IOException {
        TemplateIndex index = new TemplateIndex(ezUp, aether);
        Path indexFile = getIndexFile();

        if (!Files.exists(indexFile)) {
            Files.createFile(indexFile);
        }

        logger.info("loading templates index...");
        index.load(indexFile);

        boolean recentlyIndexed = Files.getLastModifiedTime(indexFile).toInstant()
                                       .isAfter(Instant.now().minus(1, ChronoUnit.DAYS));

        if (forceIndexing || (!recentlyIndexed && !skipIndexing)) {
            index.reindex();
            index.store(indexFile);
        }
        return index;
    }

    private static Path getIndexFile() {
        return Paths.get(System.getProperty("user.home")).resolve(".ez-up.index");
    }
}
