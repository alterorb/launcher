package net.alterorb.launcher;

import net.alterorb.launcher.alterorb.AlterOrbGame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Storage {

    private static final Path BASE_DIRECTORY = Paths.get(System.getProperty("user.home"), ".alterorb");
    private static final Path GAMEPACKS_DIRECTORY = BASE_DIRECTORY.resolve("gamepacks");

    private Storage() {
    }

    public static void initializeDirectories() throws IOException {

        if (!Files.exists(BASE_DIRECTORY)) {
            Files.createDirectories(BASE_DIRECTORY);
        }

        if (!Files.exists(GAMEPACKS_DIRECTORY)) {
            Files.createDirectories(GAMEPACKS_DIRECTORY);
        }
    }

    public static Path gamepacksDirectory() {
        return GAMEPACKS_DIRECTORY;
    }

    public static boolean gamepackExists(String alterorbGameName) {
        return Files.exists(getGamepackPath(alterorbGameName));
    }

    public static Path getGamepackPath(AlterOrbGame game) {
        return getGamepackPath(game.internalName());
    }

    public static Path getGamepackPath(String alterorbGameName) {
        return GAMEPACKS_DIRECTORY.resolve(alterorbGameName + ".jar");
    }
}
