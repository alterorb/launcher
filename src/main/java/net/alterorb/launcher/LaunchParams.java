package net.alterorb.launcher;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public record LaunchParams(
        String codeBase,
        String documentBase,
        String directLaunchGame,
        String scale
) {

    public static LaunchParams from(String[] args) {
        var optionParser = new OptionParser();
        var gameOptionSpec = optionParser.accepts("game").withRequiredArg();
        var documentBaseOptionSpec = optionParser.accepts("documentBase").withRequiredArg();
        var codeBaseOptionSpec = optionParser.accepts("codeBase").withRequiredArg();
        var scaleOptionSpec = optionParser.accepts("scale").withRequiredArg();

        OptionSet optionSet = optionParser.parse(args);

        var directLaunchGame = optionSet.valueOf(gameOptionSpec);
        var documentBase = optionSet.valueOf(documentBaseOptionSpec);
        var codeBase = optionSet.valueOf(codeBaseOptionSpec);
        var scale = optionSet.valueOf(scaleOptionSpec);

        return new LaunchParams(codeBase, documentBase, directLaunchGame, scale);
    }
}
