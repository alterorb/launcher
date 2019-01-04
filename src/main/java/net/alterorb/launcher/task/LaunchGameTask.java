package net.alterorb.launcher.task;

import lombok.extern.log4j.Log4j2;
import net.alterorb.launcher.Launcher;
import net.alterorb.launcher.alterorb.AlterorbGame;
import net.alterorb.launcher.ui.LauncherView;
import net.alterorb.launcher.ui.controller.LauncherController;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.JOptionPane;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Log4j2
@Singleton
public class LaunchGameTask implements Runnable {

    @Inject
    @Named("singleThread")
    private ExecutorService executorService;

    @Inject
    private LauncherController launcherController;

    @Inject
    private LauncherView launcherView;

    @Override
    public void run() {
        LOGGER.trace("enter");
        try {
            AlterorbGame alterorbGame = launcherView.getSelectedGame();
            Path jarLocation = Paths.get(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            List<String> cmd = new ArrayList<>();
            cmd.add("java");
            cmd.addAll(alterorbGame.getJvmExtraParams());
            cmd.add("-cp");
            cmd.add(jarLocation.toAbsolutePath().toString());
            cmd.add("net.alterorb.launcher.LaunchGame");
            cmd.add(alterorbGame.getInternalName());

            new ProcessBuilder(cmd).inheritIO()
                                   .start();
            launcherController.dispose();
            executorService.shutdown();
        } catch (IOException | URISyntaxException e) {
            LOGGER.catching(e);
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        LOGGER.trace("exit");
    }
}
