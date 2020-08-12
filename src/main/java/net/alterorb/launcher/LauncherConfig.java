package net.alterorb.launcher;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.Getter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Getter
@Singleton
public class LauncherConfig {

    private String codeBase;
    private String documentBase;
    private String directLaunchGame;

    @Inject
    public LauncherConfig() {
    }

    public void load(String[] cmdArgs) {
        OptionParser optionParser = new OptionParser();
        OptionSpec<String> gameOptionSpec = optionParser.accepts("game").withRequiredArg();
        OptionSpec<String> documentBaseOptionSpec = optionParser.accepts("documentBase").withRequiredArg();
        OptionSpec<String> codeBaseOptionSpec = optionParser.accepts("codeBase").withRequiredArg();

        OptionSet optionSet = optionParser.parse(cmdArgs);

        if (optionSet.has(gameOptionSpec)) {
            directLaunchGame = optionSet.valueOf(gameOptionSpec);
        }

        if (optionSet.has(documentBaseOptionSpec)) {
            documentBase = optionSet.valueOf(documentBaseOptionSpec);
        }

        if (optionSet.has(codeBaseOptionSpec)) {
            codeBase = optionSet.valueOf(codeBaseOptionSpec);
        }
    }
}
