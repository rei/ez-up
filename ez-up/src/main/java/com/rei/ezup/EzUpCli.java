package com.rei.ezup;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.aether.resolution.ArtifactResolutionException;

public class EzUpCli {
    public static void main(String[] args) throws ArtifactResolutionException, IOException {
        if (args.length < 2) {
            System.out.println("Missing required args!");
            printUsage();
            System.exit(1);
        }
        LinkedList<String> remaining = new LinkedList<>(Arrays.asList(args));
        
        String command = remaining.pop();
        
        if (command.equals("help")) {
            printUsage();
            System.exit(0);
        }
        
        String templateGav = remaining.pop();
        String subtemplate = null;
        
        if (!remaining.isEmpty() && !remaining.peek().startsWith("-")) {
            subtemplate = remaining.pop();
        }
        
        boolean batch = remaining.remove("-B");
        boolean noResolve = remaining.remove("-R");
        
        Map<String, String> params = new LinkedHashMap<>();
        if (!remaining.isEmpty() && remaining.pop().equals("-p")) {
            remaining.stream().map(p -> p.split("="))
                              .filter(p -> p.length == 2)
                              .forEach(p -> params.put(p[0], p[1]));
        }
        
        EzUpConfig globalConfig = new EzUpConfig(!batch, !noResolve, params);
        if (command.equals("generate")) {
            new EzUp(globalConfig).generate(templateGav, subtemplate, Paths.get(".").toAbsolutePath());
        }
    }

    private static void printUsage() {
        System.out.println("chairlift (generate|help) <template gav> [subtemplate] [opts]");
        System.out.println("Allowed Options:");
        System.out.println("-B                          non-interactive mode, will not prompt for values");
        System.out.println("-R                          don't resolve dependencies of template");
        System.out.println("-p name=value name=value    pass params to template");
    }
}
