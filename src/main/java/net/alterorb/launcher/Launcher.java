package net.alterorb.launcher;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.Semver.VersionDiff;
import net.alterorb.launcher.model.GameManifest;
import net.alterorb.launcher.applet.AlterOrbAppletContext;
import net.alterorb.launcher.applet.AlterOrbAppletStub;
import net.alterorb.launcher.event.EventDispatcher;
import net.alterorb.launcher.event.ui.LaunchGameEvent;
import net.alterorb.launcher.event.ui.ShutdownEvent;
import net.alterorb.launcher.io.HashingVoidStream;
import net.alterorb.launcher.ui.GameFrameController;
import net.alterorb.launcher.ui.LauncherController;
import net.alterorb.launcher.ui.UIConstants.Colors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.applet.Applet;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Launcher {

    public static final Semver VERSION = new Semver("4.0.0");

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);
    private static final Random RANDOM = new Random();

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(runnable -> {
        var thread = new Thread(runnable);
        thread.setName("ExecutorThread");
        thread.setUncaughtExceptionHandler(new ExceptionHandler());
        return thread;
    });

    private final AlterOrbClient client = new AlterOrbClient();
    private final LocalConfig localConfig;

    private Applet applet;

    public Launcher(LocalConfig localConfig) {
        this.localConfig = localConfig;
    }

    public void initialize() {
        LOGGER.info("Initializing AlterOrb Launcher V{}", VERSION);
        EXECUTOR_SERVICE.execute(this::startup);
        EventDispatcher.register(LaunchGameEvent.class, this::onLaunchGame);
        EventDispatcher.register(ShutdownEvent.class, this::onShutdown);
    }

    private void onLaunchGame(LaunchGameEvent event) {
        LOGGER.debug("Launching game {}", event.manifest().name());
        var launcherController = LauncherController.instance();

        launcherController.setProgressBarMessage("Launching the game...");
        try {
            if (!validateGamepack(event.manifest())) {
                launcherController.setProgressBarMessage("Downloading gamepack...");
                client.downloadGamepack(event.manifest(), LocalStorage.gamepacksDirectory(), this::onDownloadProgressUpdate);
            }
            EXECUTOR_SERVICE.execute(() -> launchApplet(event.manifest()));
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Encountered an error while downloading the gamepack", e);
            launcherController.setProgressBarMessage("There was an error while downloading the game", Colors.TEXT_ERROR);
        }
    }

    private void onDownloadProgressUpdate(int percentage) {
        LauncherController.instance().setProgressBarPercentage(percentage);
    }

    private void startup() {
        var controller = LauncherController.instance();
        try {
            LOGGER.info("Checking for launcher updates...");
            var remoteVersion = client.fetchVersion();
            LOGGER.debug("Latest version is {}", remoteVersion);
            var diff = remoteVersion.diff(VERSION);

            LOGGER.debug("Semver diff is {}", diff);
            if (diff == VersionDiff.PATCH || diff == VersionDiff.NONE) {
                var manifest = client.fetchBranchManifest(localConfig.activeBranch());
                controller.hideProgressBarAndText();
                controller.updateAvailableGames(manifest.games());
            } else {
                controller.setProgressBarMessage("Outdated launcher, download a new one at alterorb.net", Colors.TEXT_ERROR);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to fetch remote data", e);
            controller.setProgressBarMessage("There was an error while starting up the launcher", Colors.TEXT_ERROR);
        }
    }

    public void onShutdown(ShutdownEvent event) {
        LOGGER.debug("Shutting down the launcher...");
        LauncherController.instance().dispose();
        GameFrameController.instance().dispose();

        if (applet != null) {
            LOGGER.debug("Stopping the applet...");
            applet.stop();
        }
        EXECUTOR_SERVICE.shutdown();
        LOGGER.debug("Finished shutting down the launcher");
    }

    private boolean validateGamepack(GameManifest manifest) throws IOException {
        LOGGER.info("Validating the gamepack for game={}", manifest.name());
        var gamepackPath = LocalStorage.gamepackPath(manifest);

        if (!Files.exists(gamepackPath)) {
            LOGGER.debug("Gamepack does not exist, game={}", manifest.name());
            return false;
        }

        try (var stream = HashingVoidStream.create(Files.newInputStream(gamepackPath))) {
            stream.consume();

            var localSha256 = stream.hash();
            var expectedSha256 = manifest.gamepackHash();

            LOGGER.debug("Gamepack hash, local={}, expected={}", localSha256, expectedSha256);

            if (!Objects.equals(localSha256, expectedSha256)) {
                LOGGER.debug("Gamepack hash miss match, game={}", manifest.name());
                return false;
            }
        }
        return true;
    }

    private void launchApplet(GameManifest manifest) {
        LOGGER.debug("Launching game={}", manifest.name());
        var gamepackFile = LocalStorage.gamepackPath(manifest);

        try {
            var classLoader = new URLClassLoader(new URL[] {gamepackFile.toUri().toURL()}, Hook.class.getClassLoader());
            var mainClass = classLoader.loadClass(manifest.mainClass());
            applet = (Applet) mainClass.getConstructor().newInstance();

            var appletParams = Map.of(
                    "instanceid", Long.toString(RANDOM.nextLong()),
                    "gamecrc", Integer.toString(manifest.gamecrc()),
                    "gameport1", Integer.toString(manifest.serverPort()),
                    "gameport2", Integer.toString(manifest.serverPort())
            );

            var documentBase = manifest.serverHost();
            var codeBase = manifest.serverHost();

            var alterorbAppletStub = AlterOrbAppletStub.from(new AlterOrbAppletContext(), appletParams, documentBase, codeBase);
            applet.setStub(alterorbAppletStub);

            LOGGER.debug("Initializing the applet...");
            applet.init();
            applet.start();
            LOGGER.debug("Applet initialized, displaying the game view...");
            LauncherController.instance().dispose();
            GameFrameController.instance().addApplet(applet);
            GameFrameController.instance().display(manifest.allowResizing());
            LOGGER.debug("Finished launching the game");
        } catch (Exception e) {
            LOGGER.error("Encountered an error while launching the applet", e);
            LauncherController.instance().setProgressBarMessage("There was an error while launching the game", Colors.TEXT_ERROR);
        }
    }

    private static final class ExceptionHandler implements UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LOGGER.error("Uncaught exception", e);
        }
    }
}
