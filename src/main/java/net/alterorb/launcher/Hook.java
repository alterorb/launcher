package net.alterorb.launcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A class that is referenced by the patched clients for easier behavior modifications.
 */
public final class Hook {

    public static File cacheRedirect(String directory, String file) {
        try {
            var path = Storage.cacheFilePath(directory, file);

            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
            }
            return path.toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
