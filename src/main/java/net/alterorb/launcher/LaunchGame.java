package net.alterorb.launcher;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import lombok.extern.log4j.Log4j2;
import net.alterorb.launcher.alterorb.AlterorbGameConfig;
import net.alterorb.launcher.applet.AlterorbAppletStub;
import net.alterorb.launcher.ui.GameFrameView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.applet.Applet;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.jar.JarFile;

@Log4j2
public class LaunchGame {

    private static final JsonAdapter<AlterorbGameConfig> GAME_CONFIG_JSON_ADAPTER = new Moshi.Builder().build().adapter(AlterorbGameConfig.class);
    private static final String BASE_GAME_CONFIG_URL = "https://launcher.alterorb.net/configs/";
    private final StorageManager storageManager = new StorageManager();
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final String gameName;

    private LaunchGame(String gameName) {
        this.gameName = gameName;
    }

    private AlterorbGameConfig fetchGameConfig() throws IOException {
        Request request = new Builder()
                .url(BASE_GAME_CONFIG_URL + gameName + ".json")
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

    private void launch() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        if (!storageManager.gamepackExists(gameName)) {
            throw new RuntimeException("Gamepack isn't downloaded");
        }

        AlterorbGameConfig gameConfig = fetchGameConfig();
        File gamepackFile = storageManager.getGamepackPath(gameName).toFile();

        JarFile jarFile = new JarFile(gamepackFile);
        PatcherClassLoader classLoader = new PatcherClassLoader(jarFile, gameConfig.getGameshellClass(), gameConfig.getCheckhostMethod(), gameConfig.getCheckhostMethodDesc());
        Class<?> mainClass = classLoader.loadClass(gameConfig.getMainClass());
        Applet applet = (Applet) mainClass.newInstance();

        GameFrameView gameFrameView = new GameFrameView(gameConfig);
        AlterorbAppletStub alterorbAppletStub = new AlterorbAppletStub(gameConfig);
        applet.setStub(alterorbAppletStub);
        applet.init();
        applet.start();

        gameFrameView.add(applet);
        gameFrameView.setVisible(true);
        gameFrameView.setLocationRelativeTo(null);
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            LOGGER.fatal("Invalid args, args={}", Arrays.toString(args));
            System.exit(1);
        }
        try {
            new LaunchGame(args[0]).launch();
        } catch (Exception e) {
            LOGGER.catching(e);
        }
    }
}
