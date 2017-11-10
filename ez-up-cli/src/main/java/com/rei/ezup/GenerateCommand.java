package com.rei.ezup;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import com.rei.ezup.EzUpCli.MainArgs;
import com.rei.ezup.index.TemplateIndex;

@Parameters(commandDescription="generate a new project from a template or subtemplate in an existing project")
public class GenerateCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(GenerateCommand.class);

    @Parameter(description="[template gav] [subtemplate name]")
    private List<String> args = new ArrayList<>();

    @DynamicParameter(names = "-P", description = "Template Parameters")
    private Map<String, String> templateParams = new HashMap<>();

    @Parameter(names = { "-S", "-skip-reindex" }, description = "Skip reindexing of templates")
    private boolean skipReindexing = false;

    @Parameter(names = { "-F", "-force-reindex" }, description = "Force reindexing of templates")
    private boolean forceReindexing = false;

    @Override
    public void execute(JCommander jc, MainArgs mainArgs) throws Throwable {
        if (args.size() == 0 && mainArgs.nonInteractive) {
            throw new IllegalArgumentException("template gav must be specified in non-interactive mode");
        }

        EzUpContext ctx = EzUpContext.fromArgs(mainArgs, templateParams);
        String templateGav = args.size() == 0 ? chooseTemplate(ctx) : args.get(0);
        String subtemplate = args.size() == 2 ? args.get(1) : null;
        String readme = ctx.getEzUp().generate(templateGav, subtemplate, Paths.get("."));

        logger.info("Successfully generated {}template: {}", subtemplate != null ? "sub" : "", templateGav);
        logger.info(readme == null ? "" : readme);
    }

    private String chooseTemplate(EzUpContext ctx) throws IOException {
        TemplateIndex index = ctx.getTemplateIndex(skipReindexing, forceReindexing);

        logger.info("Select a Template: ");

        List<TemplateInfo> indexedTemplates = index.getIndexedTemplates();
        for (int i = 0; i < indexedTemplates.size(); i++) {
            TemplateInfo ti = indexedTemplates.get(i);
            logger.info("[{}] {} - {}", i, ti.getName(), ti.getDescription());
        }

        Integer selection = Integer.valueOf(System.console().readLine("Choose Template: "));
        if (selection > indexedTemplates.size() - 1) {
            throw new IllegalArgumentException("invalid selection");
        }
        return indexedTemplates.get(selection).getArtifact().toString();
    }
}
