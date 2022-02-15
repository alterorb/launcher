package net.alterorb.launcher;

import net.alterorb.launcher.alterorb.AlterOrbGame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Storage {

    private static final Path BASE_DIRECTORY = Paths.get(System.getProperty("user.home"), ".alterorb");
    private static final Path GAMEPACKS_DIRECTORY = BASE_DIRECTORY.resolve("gamepacks");
    private static final Path CACHES_DIRECTORY = BASE_DIRECTORY.resolve("caches");

    private Storage() {
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

    public static Path cacheFilePath(String subDirectory, String file) {
        if (subDirectory != null) {
            return CACHES_DIRECTORY.resolve(subDirectory).resolve(file);
        }
        return CACHES_DIRECTORY.resolve(file);
    }

    public static Path gamepackPath(AlterOrbGame game) {
        return gamepackPath(game.internalName());
    }

    public static Path gamepackPath(String alterorbGameName) {
        return GAMEPACKS_DIRECTORY.resolve(alterorbGameName + ".jar");
    }
}
