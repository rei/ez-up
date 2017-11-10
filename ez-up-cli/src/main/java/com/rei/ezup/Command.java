package com.rei.ezup;

import com.beust.jcommander.JCommander;

import com.rei.ezup.EzUpCli.MainArgs;

public interface Command {
    void execute(JCommander jc, MainArgs mainArgs) throws Throwable;
}