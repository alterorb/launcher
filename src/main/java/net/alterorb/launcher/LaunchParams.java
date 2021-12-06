package net.alterorb.launcher;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public record LaunchParams(
        String codeBase,
        String documentBase,
        String directLaunchGame
) {

    public static LaunchParams from(String[] args) {
        var optionParser = new OptionParser();
        var gameOptionSpec = optionParser.accepts("game").withRequiredArg();
        var documentBaseOptionSpec = optionParser.accepts("documentBase").withRequiredArg();
        var codeBaseOptionSpec = optionParser.accepts("codeBase").withRequiredArg();

        OptionSet optionSet = optionParser.parse(args);

        var directLaunchGame = optionSet.valueOf(gameOptionSpec);
        var documentBase = optionSet.valueOf(documentBaseOptionSpec);
        var codeBase = optionSet.valueOf(codeBaseOptionSpec);

        return new LaunchParams(directLaunchGame, documentBase, codeBase);
    }
}
