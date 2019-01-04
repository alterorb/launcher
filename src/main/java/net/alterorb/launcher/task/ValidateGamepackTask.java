package net.alterorb.launcher.task;

import lombok.extern.log4j.Log4j2;
import net.alterorb.launcher.StorageManager;
import net.alterorb.launcher.alterorb.AlterorbGame;
import net.alterorb.launcher.ui.LauncherView;
import okio.BufferedSource;
import okio.HashingSink;
import okio.Okio;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.JOptionPane;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

@Log4j2
@Singleton
public class ValidateGamepackTask implements Runnable {

    @Inject
    private StorageManager storageManager;

    @Inject
    @Named("singleThread")
    private ExecutorService executorService;

    @Inject
    private LauncherView launcherView;

    @Inject
    private DownloadGamepackTask downloadGamepackTask;

    @Inject
    private LaunchGameTask launchGameTask;

    @Override
    public void run() {
        LOGGER.trace("enter");
        try {
            AlterorbGame alterorbGame = launcherView.getSelectedGame();

            Path gamepackPath = storageManager.getGamepackPath(alterorbGame);

            if (!Files.exists(gamepackPath)) {
                executorService.submit(downloadGamepackTask);
                return;
            }

            try (HashingSink hashingSink = HashingSink.sha256(Okio.blackhole());
                    BufferedSource source = Okio.buffer(Okio.source(gamepackPath))) {
                source.readAll(hashingSink);

                String localSha256 = hashingSink.hash().hex();
                String expectedSha256 = alterorbGame.getGamepackHash();

                LOGGER.debug("local={}, expected={}", localSha256, expectedSha256);
                if (Objects.equals(localSha256, expectedSha256)) {
                    executorService.submit(launchGameTask);
                } else {
                    executorService.submit(downloadGamepackTask);
                }
            }
        } catch (IOException e) {
            LOGGER.catching(e);
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        LOGGER.trace("exit");
    }
}
