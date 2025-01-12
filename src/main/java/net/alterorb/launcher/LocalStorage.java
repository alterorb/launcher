package net.alterorb.launcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.alterorb.launcher.alterorb.GameManifest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public final class LocalStorage {

    private static final Path BASE_DIRECTORY = Paths.get(System.getProperty("user.home"), ".alterorb");
    private static final Path GAMEPACKS_DIRECTORY = BASE_DIRECTORY.resolve("gamepacks");
    private static final Path CACHES_DIRECTORY = BASE_DIRECTORY.resolve("caches");
    private static final Path CONFIG_PATH = BASE_DIRECTORY.resolve("localconfig.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private LocalStorage() {
    }

    public static Path gamepacksDirectory() {
        return GAMEPACKS_DIRECTORY;
    }

    public static void initializeDirectories() throws IOException {

        if (!Files.exists(BASE_DIRECTORY)) {
            Files.createDirectories(BASE_DIRECTORY);
        }

        if (!Files.exists(GAMEPACKS_DIRECTORY)) {
            Files.createDirectories(GAMEPACKS_DIRECTORY);
        }

        if (!Files.exists(CACHES_DIRECTORY)) {
            Files.createDirectories(CACHES_DIRECTORY);
        }
    }

    public static LocalConfig loadLocalConfig() throws IOException {
        var configPath = BASE_DIRECTORY.resolve("localconfig.json");

        if (Files.notExists(configPath)) {
            return null;
        }

        try (var file = Files.newBufferedReader(configPath)) {
            return GSON.fromJson(file, LocalConfig.class);
        }
    }

    public static void saveLocalConfig(LocalConfig localConfig) throws IOException {
        try (var writer = Files.newBufferedWriter(CONFIG_PATH, CREATE, WRITE)) {
            GSON.toJson(localConfig, writer);
        }
    }

    public static Path cacheFilePath(String subDirectory, String file) {
        if (subDirectory != null) {
            return CACHES_DIRECTORY.resolve(subDirectory).resolve(file);
        }
        return CACHES_DIRECTORY.resolve(file);
    }

    public static Path gamepackPath(GameManifest manifest) {
        return gamepackPath(manifest.gamepack());
    }

    private static Path gamepackPath(String jarName) {
        return GAMEPACKS_DIRECTORY.resolve(jarName + ".jar");
    }
}
