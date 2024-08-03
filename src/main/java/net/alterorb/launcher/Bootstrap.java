package net.alterorb.launcher;

import net.alterorb.launcher.ui.LauncherController;
import net.alterorb.launcher.ui.UIConstants;
import net.alterorb.launcher.ui.UIConstants.Colors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Bootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        LOGGER.info("Bootstrapping the launcher...");
        var localConfig = LaunchParams.from(args);

        if (localConfig.scale() != null) {
            System.setProperty("sun.java2d.uiScale", localConfig.scale());
        }
        UIConstants.loadResources();
        var launcher = Launcher.create(localConfig);
        var controller = LauncherController.instance();

        controller.display();
        try {
            launcher.initialize();
        } catch (IOException e) {
            controller.setProgressBarMessage("There was an error while bootstrapping the launcher", Colors.TEXT_ERROR);
            LOGGER.error("Failed to validate launcher version", e);
        }
    }
}
