package com.rei.ezup;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import com.rei.ezup.EzUpCli.MainArgs;
import com.rei.ezup.index.TemplateIndex;

@Parameters(commandDescription = "re-index and list known templates")
public class ListTemplatesCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(ListTemplatesCommand.class);

    @Parameter(names = { "-S", "-skip-reindex" }, description = "Skip reindexing of templates")
    private boolean skipReindexing = false;

    @Parameter(names = { "-F", "-force-reindex" }, description = "Force reindexing of templates")
    private boolean forceReindexing = false;

    @Override
    public void execute(JCommander jc, MainArgs mainArgs) throws Throwable {
        EzUpContext ctx = EzUpContext.fromArgs(mainArgs, Collections.emptyMap());
        TemplateIndex index = ctx.getTemplateIndex(skipReindexing, forceReindexing);

        logger.info("");

        index.getIndexedTemplates().forEach(ti -> {
            logger.info("{} - {}", ti.getName(), ti.getDescription());
            logger.info("  {}", ti.getArtifact());
            if (!ti.getSubtemplates().isEmpty()) {
                logger.info("  subtemplates: {}", String.join(", ", ti.getSubtemplates()));
            }
            logger.info("");
        });
    }
}
