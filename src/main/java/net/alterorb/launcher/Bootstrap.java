package net.alterorb.launcher;

import com.google.gson.JsonParseException;
import net.alterorb.launcher.ui.LauncherController;
import net.alterorb.launcher.ui.UIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JOptionPane;
import java.io.IOException;

public class Bootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        LOGGER.info("Bootstrapping the launcher...");

        AssetLoader.asyncLoadAssets();
        UIConstants.loadResources();

        try {
            LocalStorage.initializeDirectories();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to initialize the launcher's storage, check if you have the correct permissions or see logs for more details.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            LOGGER.error("Failed to initialize the launcher", e);
            return;
        }

        LocalConfig localConfig;
        try {
            localConfig = LocalStorage.loadLocalConfig();
        } catch (IOException e) {
            localConfig = LocalConfig.DEFAULT;
            LOGGER.warn("Failed to load local config, falling back to DEFAULT", e);
        } catch (JsonParseException e) {
            localConfig = LocalConfig.DEFAULT;
            resetLocalConfig();
            LOGGER.warn("Malformed local config json, resetting DEFAULT", e);
        }

        if (localConfig == null || LocalConfig.isInvalid(localConfig)) {
            LOGGER.debug("Local config is null or invalid, replacing with DEFAULT.");
            localConfig = LocalConfig.DEFAULT;
            resetLocalConfig();
        }

        System.setProperty("sun.java2d.uiScale", Float.toString(localConfig.uiScalingFactor()));

        var launcher = new Launcher(localConfig);
        var controller = LauncherController.instance();

        controller.display();
        launcher.initialize();
    }

    private static void resetLocalConfig() {
        try {
            LocalStorage.saveLocalConfig(LocalConfig.DEFAULT);
        } catch (IOException e) {
            LOGGER.warn("Failed to save local config", e);
        }
    }
}
