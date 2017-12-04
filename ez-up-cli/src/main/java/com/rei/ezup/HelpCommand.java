package com.rei.ezup;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import com.rei.ezup.EzUpCli.MainArgs;

@Parameters(commandDescription="display help for commands")
public class HelpCommand implements Command {

    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Override
    public void execute(JCommander jc, MainArgs mainArgs) {
        if (!parameters.isEmpty()) {
            jc.usage(parameters.get(0));
        } else {
            jc.usage();
        }
    }

}
