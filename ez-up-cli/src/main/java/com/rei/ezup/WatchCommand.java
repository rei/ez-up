package com.rei.ezup;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription="generate from an in-development template and watch the template for changes")
public class WatchCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(WatchCommand.class);
    private static final String DIR_PARAMS_DETAILS = " (at least one of '-template-dir' or '-project-dir' MUST be specified)";

    @Parameter(description="[template gav] [subtemplate name]")
    private List<String> args = new ArrayList<>();

    @DynamicParameter(names = "-P", description = "Template Parameters")
    private Map<String, String> templateParams = new HashMap<>();

    @Parameter(names = { "-project-dir" }, description = "Directory of destination project" + DIR_PARAMS_DETAILS)
    private String projectDir;

    @Parameter(names = { "-template-dir" }, description = "Directory of template" + DIR_PARAMS_DETAILS)
    private String templateDir;

    @Override
    public void execute(JCommander jc, EzUpCli.MainArgs mainArgs) throws Throwable {
        EzUpContext ctx = EzUpContext.fromArgs(mainArgs, templateParams);
        if (this.projectDir == null && this.templateDir == null) {
            throw new IllegalArgumentException(DIR_PARAMS_DETAILS);
        }

        Path projectDir = Optional.ofNullable(this.projectDir).map(Paths::get).orElseGet(() -> Paths.get("."));
        Path templateDir = Optional.ofNullable(this.templateDir).map(Paths::get).orElseGet(() -> Paths.get("."));

        CompletableFuture<Void> watch = ctx.getEzUp().generateAndWatch(templateDir, projectDir);
        logger.info("initial template generated, watching for changes");

        watch.join();
    }
}
