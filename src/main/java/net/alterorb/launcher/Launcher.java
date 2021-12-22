package net.alterorb.launcher;

import com.google.gson.GsonBuilder;
import net.alterorb.launcher.alterorb.AlterOrbGame;
import net.alterorb.launcher.alterorb.AlterOrbGame.AlterOrbGameAdapter;
import net.alterorb.launcher.alterorb.RemoteConfig;
import net.alterorb.launcher.alterorb.RemoteConfig.RemoteConfigAdapter;
import net.alterorb.launcher.applet.AlterOrbAppletContext;
import net.alterorb.launcher.applet.AlterOrbAppletStub;
import net.alterorb.launcher.event.EventDispatcher;
import net.alterorb.launcher.event.ui.LaunchGameEvent;
import net.alterorb.launcher.event.ui.ShutdownEvent;
import net.alterorb.launcher.ui.GameFrameController;
import net.alterorb.launcher.ui.LauncherController;
import net.alterorb.launcher.ui.UIConstants.Colors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.applet.Applet;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    private static final String BASE_URL = "https://static.alterorb.net/launcher/v3/";
    private static final String CONFIG_URL = BASE_URL + "config.json";
    private static final String VERSION = "3.0.0";

    private static final Random RANDOM = new Random();
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
                                                            .connectTimeout(Duration.ofSeconds(10))
                                                            .build();

    private final LaunchParams launchParams;

    private RemoteConfig remoteConfig;
    private Applet applet;

    private Launcher(LaunchParams launchParams) {
        this.launchParams = launchParams;
    }

    public static Launcher create(LaunchParams launchParams) {
        return new Launcher(launchParams);
    }

    public void initialize() throws IOException {
        LOGGER.info("Initializing AlterOrb Launcher V{}", VERSION);
        Storage.initializeDirectories();
        EXECUTOR_SERVICE.execute(this::startup);
        EventDispatcher.register(LaunchGameEvent.class, this::onLaunchGame);
        EventDispatcher.register(ShutdownEvent.class, this::onShutdown);
    }

    private void onLaunchGame(LaunchGameEvent event) {
        LOGGER.debug("Launching game {}", event.game().name());
        LauncherController.instance().setProgressBarMessage("Launching the game...");
        try {
            if (!validateGamepack(event.game())) {
                downloadGamepack(event.game());
            }
            launchApplet(event.game());
            LauncherController.instance().dispose();
        } catch (Exception e) {
            LOGGER.error("Encountered an error while launching the game", e);
            LauncherController.instance().setProgressBarMessage("There was an error while launching the game", Colors.TEXT_ERROR);
        }
    }

    private void startup() {
        var controller = LauncherController.instance();
        try {
            remoteConfig = fetchRemoteConfig();

            if (remoteConfig.version().equals(VERSION)) {
                controller.hideProgressBarAndText();
                controller.updateAvailableGames(remoteConfig.games());
            } else {
                controller.setProgressBarMessage("Outdated launcher, download a new one at alterorb.net", Colors.TEXT_ERROR);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to load remote config", e);
            controller.setProgressBarMessage("There was an error while starting up the launcher", Colors.TEXT_ERROR);
        }
    }

    private RemoteConfig fetchRemoteConfig() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                                 .GET()
                                 .uri(URI.create(CONFIG_URL))
                                 .build();

        var response = HTTP_CLIENT.send(request, BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch remote config, response code: " + response.statusCode());
        }
        var gson = new GsonBuilder()
                .registerTypeAdapter(RemoteConfig.class, new RemoteConfigAdapter())
                .registerTypeAdapter(AlterOrbGame.class, new AlterOrbGameAdapter())
                .create();

        return gson.fromJson(new InputStreamReader(response.body()), RemoteConfig.class);
    }

    public void onShutdown(ShutdownEvent event) {
        LOGGER.debug("Shutting down the launcher...");
        LauncherController.instance().dispose();
        GameFrameController.instance().dispose();

        if (applet != null) {
            LOGGER.debug("Stopping the applet...");
            applet.stop();
        }
        LOGGER.debug("Finished shutting down the launcher");
    }

    private boolean validateGamepack(AlterOrbGame game) throws IOException {
        LOGGER.debug("Validating the gamepack for game={}", game.internalName());
        var gamepackPath = Storage.getGamepackPath(game);

        if (!Files.exists(gamepackPath)) {
            LOGGER.info("Gamepack does not exist, game={}", game.internalName());
            return false;
        }

        try (var stream = HashingVoidStream.create(Files.newInputStream(gamepackPath))) {
            stream.consume();

            var localSha256 = stream.hash();
            var expectedSha256 = game.gamepackHash();

            LOGGER.debug("Gamepack hash, local={}, expected={}", localSha256, expectedSha256);

            if (!Objects.equals(localSha256, expectedSha256)) {
                LOGGER.info("Gamepack hash miss match, game={}", game.internalName());
                return false;
            }
        }
        return false;
    }

    private void downloadGamepack(AlterOrbGame game) throws IOException, InterruptedException {
        LOGGER.debug("Downloading gamepack for game {}", game.name());
        var httpRequest = HttpRequest.newBuilder()
                                     .GET()
                                     .uri(URI.create(Launcher.BASE_URL + "gamepacks/" + game.internalName() + ".jar"))
                                     .build();

        LauncherController.instance().setProgressBarMessage("Downloading gamepack...");

        var gamepackPath = Storage.getGamepackPath(game);
        Files.createDirectories(gamepackPath);
        var response = HTTP_CLIENT.send(httpRequest, BodyHandlers.ofFileDownload(gamepackPath));

        if (response.statusCode() != 200) {
            throw new IOException("Failed to download gamepack");
        }
    }

    private void launchApplet(AlterOrbGame game) throws Exception {
        LOGGER.debug("Launching game={}", game.internalName());
        var gamepackFile = Storage.getGamepackPath(game.internalName());

        var classLoader = new URLClassLoader(new URL[] {gamepackFile.toUri().toURL()});
        var mainClass = classLoader.loadClass(game.mainClass());
        applet = (Applet) mainClass.getConstructor().newInstance();

        Map<String, String> params = Map.of(
                "instanceid", Long.toString(RANDOM.nextLong()),
                "gamecrc", Integer.toString(game.gamecrc())
        );
        var alterorbAppletStub = AlterOrbAppletStub.from(new AlterOrbAppletContext(), params, launchParams.documentBase(), launchParams.codeBase());
        applet.setStub(alterorbAppletStub);

        LOGGER.debug("Initializing the applet...");
        applet.init();
        applet.start();
        LOGGER.debug("Applet initialized, displaying the game view...");
        GameFrameController.instance().addApplet(applet);
        GameFrameController.instance().display();
        LOGGER.debug("Finished launching the game");
    }
}
