package net.alterorb.launcher;

import com.bulenkov.darcula.DarculaLaf;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import lombok.extern.log4j.Log4j2;
import net.alterorb.launcher.ProgressListenableSource.ProgressListener;
import net.alterorb.launcher.alterorb.AlterorbGame;
import net.alterorb.launcher.ui.ProgressBarView;
import net.alterorb.launcher.ui.UIConstants;
import net.alterorb.launcher.ui.UIConstants.Colors;
import net.alterorb.launcher.ui.UIConstants.Fonts;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.HashingSink;
import okio.Okio;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

@Log4j2
public class Launcher {

    private static final String BASE_URL = "https://launcher.alterorb.net/";
    private static final String VERSION = "1.0";

    private final Moshi moshi = new Moshi.Builder().build();
    private final ProgressBarView progressBarView = new ProgressBarView();
    private final StorageManager storageManager = new StorageManager();
    private final OkHttpClient okHttpClient = new OkHttpClient();

    private AlterorbGame[] availableGames;

    private void initialize() throws IOException {
        storageManager.initializeDirectories();
    }

    private void launch() {
        progressBarView.setVisible(true);
        try {

            if (!checkVersion()) {
                progressBarView.switchToErrorState("A new version of the launcher is available");
                return;
            }
            initialize();
            progressBarView.setText("Fetching game list");
            fetchGameList();

            AlterorbGame orbdefence = availableGames[0];

            progressBarView.setText("Validating gamepack");
            if (!isGamepackValid(orbdefence)) {
                LOGGER.debug("Gamepack is invalid, downloading");
                progressBarView.setText("Downloading " + orbdefence.getName());
                downloadGamepack(orbdefence);
            }
            progressBarView.setText("Launching " + orbdefence.getName());
            launchGame(orbdefence);
        } catch (Exception e) {
            progressBarView.switchToErrorState("An error occurred");
            LOGGER.catching(e);
        }
    }

    private boolean checkVersion() throws IOException {
        Request request = new Builder()
                .url(BASE_URL + "version.json")
                .build();

        Response response = okHttpClient.newCall(request)
                                        .execute();

        if (!response.isSuccessful()) {
            throw new IOException("Failed to validate the launcher's version");
        }
        try (ResponseBody body = response.body()) {

            if (body == null) {
                throw new IOException("Empty response body");
            }
            JsonAdapter<String> jsonAdapter = moshi.adapter(String.class);
            String remoteVersion = jsonAdapter.fromJson(body.string());

            if (!Objects.equals(VERSION, remoteVersion)) {
                return false;
            }
        }
        return true;
    }

    private void fetchGameList() throws IOException {
        Request request = new Builder()
                .url(BASE_URL + "available-games.json")
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

            JsonAdapter<AlterorbGame[]> jsonAdapter = moshi.adapter(AlterorbGame[].class);

            availableGames = jsonAdapter.fromJson(responseBody.source());
            LOGGER.debug("Available games={}", Arrays.toString(availableGames));
        }
    }

    private boolean isGamepackValid(AlterorbGame alterorbGame) throws IOException {
        Path gamepackPath = storageManager.getGamepackPath(alterorbGame);

        if (!Files.exists(gamepackPath)) {
            return false;
        }

        try (HashingSink hashingSink = HashingSink.sha256(Okio.blackhole());
                BufferedSource source = Okio.buffer(Okio.source(gamepackPath))) {
            source.readAll(hashingSink);

            String localSha256 = hashingSink.hash().hex();
            String expectedSha256 = alterorbGame.getGamepackHash();

            LOGGER.debug("local={}, expected={}", localSha256, expectedSha256);
            return Objects.equals(localSha256, expectedSha256);
        }
    }

    private void downloadGamepack(AlterorbGame alterorbGame) throws IOException {
        Request request = new Builder()
                .url(BASE_URL + "gamepacks/" + alterorbGame.getInternalName() + ".jar")
                .build();

        Response response = okHttpClient.newCall(request)
                                        .execute();

        if (!response.isSuccessful()) {
            throw new RuntimeException("Failed to fetch gamepack");
        }

        try (ResponseBody responseBody = response.body()) {

            if (responseBody == null) {
                throw new IOException("Empty response body");
            }
            Path gamepackPath = storageManager.getGamepackPath(alterorbGame);
            ProgressListener progressListener = (bytesRead, length, done) -> {
                long percentage = bytesRead * 100 / length;
                percentage = Math.min(100, Math.max(0, percentage));
                progressBarView.setProgressBarPercentage((int) percentage);
            };

            try (BufferedSink sink = Okio.buffer(Okio.sink(gamepackPath));
                    ProgressListenableSource source = new ProgressListenableSource(responseBody.source(), responseBody.contentLength(), progressListener)) {
                sink.writeAll(source);
            }
        }
    }

    private void launchGame(AlterorbGame alterorbGame) throws URISyntaxException, IOException {
        Path jarLocation = Paths.get(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        LOGGER.debug("jarLocation={}", jarLocation);

        List<String> cmd = new ArrayList<>();
        cmd.add("java");
        cmd.addAll(alterorbGame.getJvmExtraParams());
        cmd.add("-cp");
        cmd.add(jarLocation.toAbsolutePath().toString());
        cmd.add("net.alterorb.launcher.LaunchGame");
        cmd.add(alterorbGame.getInternalName());

        LOGGER.debug("Launch cmd={}", cmd);

        new ProcessBuilder(cmd).inheritIO()
                               .start();
        LOGGER.debug("Launched the game");
        progressBarView.setVisible(false);
        progressBarView.dispose();
    }

    private static void registerFont(InputStream stream) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
            GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();

            environment.registerFont(font);
        } catch (FontFormatException | IOException e) {
            LOGGER.error("Failed to register font", e);
        }
    }

    private static void setDefaultFont(FontUIResource font) {
        Enumeration keys = UIManager.getDefaults().keys();

        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);

            if (value instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }

    public static void main(String[] args) {
        UIManager.getFont("Label.font"); // Fixes darcula NPE when on linux, https://github.com/bulenkov/iconloader/issues/14
        UIManager.put("ToolTip.background", Colors.DARCULA);

        try {
            UIManager.setLookAndFeel(new DarculaLaf());
        } catch (Exception e) {
            LOGGER.warn("Failed to load darcula, falling back to system's look and feel.");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Cannot happen
            }
        }
        Class<Launcher> clazz = Launcher.class;

        registerFont(clazz.getResourceAsStream("/fonts/opensans.ttf"));
        registerFont(clazz.getResourceAsStream("/fonts/opensans-bold.ttf"));

        setDefaultFont(new FontUIResource(Fonts.OPEN_SANS_13));

        new Launcher().launch();
    }
}
