package net.alterorb.launcher;

import com.pivovarit.function.ThrowingFunction;
import com.pivovarit.function.ThrowingSupplier;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory;
import lombok.extern.slf4j.Slf4j;
import net.alterorb.launcher.ProgressListenableSource.ProgressListener;
import net.alterorb.launcher.alterorb.AlterorbGame;
import net.alterorb.launcher.alterorb.AvailableGame;
import net.alterorb.launcher.applet.AlterorbAppletContext;
import net.alterorb.launcher.applet.AlterorbAppletStub;
import net.alterorb.launcher.patcher.Patch;
import net.alterorb.launcher.patcher.PatcherClassLoader;
import net.alterorb.launcher.patcher.impl.CheckhostPatch;
import net.alterorb.launcher.patcher.impl.LoginPublicKeyPatch;
import net.alterorb.launcher.patcher.impl.MouseInputPatch;
import net.alterorb.launcher.ui.GameFrameController;
import net.alterorb.launcher.ui.LauncherController;
import net.alterorb.launcher.ui.UIConstants.Colors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.HashingSink;
import okio.Okio;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.applet.Applet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarFile;

@Slf4j
@Singleton
public class Launcher {

    public static final String BASE_URL = "https://static.alterorb.net/launcher/v2/";
    public static final String VERSION = "2.1";

    private static final String BASE_GAME_CONFIG_URL = BASE_URL + "config/";
    private static final JsonAdapter<AlterorbGame> GAME_CONFIG_JSON_ADAPTER = new Moshi.Builder()
            .add(PolymorphicJsonAdapterFactory.of(Patch.class, "type")
                                              .withSubtype(CheckhostPatch.class, "checkhost")
                                              .withSubtype(MouseInputPatch.class, "mouseinput")
                                              .withSubtype(LoginPublicKeyPatch.class, "loginpubkey"))
            .build().adapter(AlterorbGame.class);

    private final DiscordIntegration discordIntegration;
    private final GameFrameController gameFrameController;
    private final LauncherController launcherController;
    private final LauncherConfig launcherConfig;
    private final OkHttpClient okHttpClient;
    private final Storage storage;
    private final Moshi moshi;

    private Applet applet;

    @Inject
    public Launcher(DiscordIntegration discordIntegration, GameFrameController gameFrameController, LauncherController launcherController, LauncherConfig launcherConfig, OkHttpClient okHttpClient,
            Storage storage, Moshi moshi) {
        this.discordIntegration = discordIntegration;
        this.gameFrameController = gameFrameController;
        this.launcherController = launcherController;
        this.launcherConfig = launcherConfig;
        this.okHttpClient = okHttpClient;
        this.storage = storage;
        this.moshi = moshi;
    }

    public void initialize() {
        LOGGER.info("Initializing AlterOrb Launcher V{}", VERSION);
        launcherController.hideProgressBarAndText();

        try {
            storage.initializeDirectories();
        } catch (IOException e) {
            LOGGER.error("Failed to create directories", e);
        }
        CompletableFuture.runAsync(discordIntegration::initialize);

        String directLaunchGame = launcherConfig.getDirectLaunchGame();

        if (directLaunchGame != null) {
            LOGGER.info("Direct launching game={}", directLaunchGame);
            launchGame(directLaunchGame);
        } else {
            CompletableFuture.supplyAsync(ThrowingSupplier.sneaky(this::fetchGameList))
                             .thenAcceptAsync(launcherController::updateAvailableGames)
                             .exceptionally(this::handleError);
        }
    }

    public void shutdown() {
        LOGGER.debug("Shutting down the launcher...");
        launcherController.dispose();
        gameFrameController.dispose();
        okHttpClient.dispatcher().executorService().shutdown();
        okHttpClient.connectionPool().evictAll();

        if (applet != null) {
            LOGGER.debug("Stopping the applet...");
            applet.stop();
        }
        discordIntegration.shutdown();
        LOGGER.debug("Finished shutting down the launcher");
    }

    public void launchGame(String gameInternalName) {

        if (gameInternalName == null) {
            LOGGER.warn("Launching null game?");
            return;
        }
        try {
            launcherController.setProgressBarMessage("Launching the game...");
            CompletableFuture.supplyAsync(() -> gameInternalName)
                             .thenApplyAsync(ThrowingFunction.sneaky(this::fetchGameConfig))
                             .thenApplyAsync(ThrowingFunction.sneaky(this::validateGamepack))
                             .thenApplyAsync(ThrowingFunction.sneaky(this::launchApplet))
                             .thenAccept(discordIntegration::updateRichPresence)
                             .thenAccept(v -> launcherController.dispose())
                             .exceptionally(this::handleError);
        } catch (Exception e) {
            handleError(e);
        }
    }

    private Void handleError(Throwable e) {
        LOGGER.error("Encountered an error while executing async task", e);
        launcherController.setProgressBarMessage("There was an error while launching the game", Colors.TEXT_ERROR);
        return null;
    }

    private List<AvailableGame> fetchGameList() throws IOException {
        LOGGER.debug("Fetching game list...");
        Request request = new Builder()
                .url(Launcher.BASE_URL + "available-games.json")
                .build();

        Response response = okHttpClient.newCall(request)
                                        .execute();

        if (!response.isSuccessful()) {
            throw new RuntimeException("Failed to fetch game list");
        }
        try (ResponseBody responseBody = response.body()) {

            if (responseBody == null) {
                throw new IOException("Empty response body");
            }

            JsonAdapter<List<AvailableGame>> jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, AvailableGame.class));

            return jsonAdapter.fromJson(responseBody.source());
        }
    }

    private AlterorbGame validateGamepack(AlterorbGame game) throws IOException {
        LOGGER.debug("Validating the gamepack for game={}", game.getInternalName());
        Path gamepackPath = storage.getGamepackPath(game);

        if (!Files.exists(gamepackPath)) {
            LOGGER.info("Gamepack does not exist, game={}", game.getInternalName());
            downloadGamepack(game);
            return game;
        }

        try (HashingSink hashingSink = HashingSink.sha256(Okio.blackhole());
                BufferedSource source = Okio.buffer(Okio.source(gamepackPath))) {
            source.readAll(hashingSink);

            String localSha256 = hashingSink.hash().hex();
            String expectedSha256 = game.getGamepackHash();

            LOGGER.debug("Gamepack hash, local={}, expected={}", localSha256, expectedSha256);

            if (!Objects.equals(localSha256, expectedSha256)) {
                LOGGER.info("Gamepack hash miss match, game={}", game.getInternalName());
                downloadGamepack(game);
            }
        }
        return game;
    }

    private void downloadGamepack(AlterorbGame game) throws IOException {
        Request request = new Builder()
                .url(Launcher.BASE_URL + "gamepacks/" + game.getInternalName() + ".jar")
                .build();

        Response response = okHttpClient.newCall(request)
                                        .execute();

        if (!response.isSuccessful()) {
            throw new IOException("Failed to download gamepack");
        }

        try (ResponseBody responseBody = response.body()) {

            if (responseBody == null) {
                throw new IOException("Empty response body");
            }
            Path gamepackPath = storage.getGamepackPath(game);
            ProgressListener progressListener = (bytesRead, length, done) -> {
                long percentage = bytesRead * 100 / length;
                percentage = Math.min(100, Math.max(0, percentage));
                launcherController.updateProgressBar((int) percentage);
            };
            launcherController.setProgressBarMessage("Downloading gamepack...");

            try (BufferedSink sink = Okio.buffer(Okio.sink(gamepackPath));
                    ProgressListenableSource source = new ProgressListenableSource(responseBody.source(), responseBody.contentLength(), progressListener)) {
                sink.writeAll(source);
            }
        }
    }

    private AlterorbGame fetchGameConfig(String internalName) throws IOException {
        LOGGER.debug("Fetching config for game={}", internalName);
        Request request = new Builder()
                .url(BASE_GAME_CONFIG_URL + internalName + ".json")
                .build();

        Response response = okHttpClient.newCall(request)
                                        .execute();

        if (!response.isSuccessful()) {
            throw new IOException("Failed to fetch game config, likely unsupported game");
        }
        try (ResponseBody body = response.body()) {

            if (body == null) {
                throw new IOException("Response body is null");
            }
            return GAME_CONFIG_JSON_ADAPTER.fromJson(body.source());
        }
    }

    private AlterorbGame launchApplet(AlterorbGame game) throws Exception {
        LOGGER.debug("Launching game={}", game.getInternalName());
        File gamepackFile = storage.getGamepackPath(game.getInternalName()).toFile();

        JarFile jarFile = new JarFile(gamepackFile);
        PatcherClassLoader classLoader = new PatcherClassLoader(jarFile, game.getPatches());
        Class<?> mainClass = classLoader.loadClass(game.getMainClass());
        applet = (Applet) mainClass.getConstructor().newInstance();

        AlterorbAppletContext appletContext = new AlterorbAppletContext(this);
        AlterorbAppletStub alterorbAppletStub = new AlterorbAppletStub(game, appletContext, launcherConfig.getDocumentBase(), launcherConfig.getCodeBase());
        applet.setStub(alterorbAppletStub);

        LOGGER.debug("Initializing the applet...");
        applet.init();
        applet.start();
        LOGGER.debug("Applet initialized, displaying the game view...");
        gameFrameController.addApplet(applet);
        gameFrameController.display();
        LOGGER.debug("Finished launching the game");
        return game;
    }
}
