package com.rei.ezup;

import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.ImmutableMap;

public class EzUpCli {
    public static void main(String[] args) {
        System.setProperty("logging.level", ArrayUtils.contains(args, "-debug") ? "debug" : "info");

        Map<String, Command> commands = ImmutableMap.<String, Command>builder()
                                                .put("help", new HelpCommand())
                                                .put("generate", new GenerateCommand())
                                                .put("list", new ListTemplatesCommand())
                                                .build();


        MainArgs mainArgs = new MainArgs();
        JCommander jc = new JCommander(mainArgs);
        jc.setColumnSize(120);
        commands.forEach(jc::addCommand);
        try {
            jc.parse(args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        if (jc.getParsedCommand() == null) {
            jc.usage();
            System.exit(1);
        }

        try {
            commands.get(jc.getParsedCommand()).execute(jc, mainArgs);
        } catch (Throwable t) {
            if (mainArgs.debug) {
                t.printStackTrace(System.out);
            } else {
                System.out.println(t.getMessage());
            }
        }
    }

    public static class MainArgs {
        @Parameter(names = "-debug", description = "Debug mode")
        public boolean debug = false;

        @Parameter(names = "-B", description = "Non-interactive mode")
        public boolean nonInteractive = false;
    }
}
