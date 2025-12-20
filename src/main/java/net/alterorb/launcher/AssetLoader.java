package net.alterorb.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AssetLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetLoader.class);

    private static final Map<String, Future<BufferedImage>> THUMBNAIL_ASSETS = new ConcurrentHashMap<>();

    public static void asyncLoadAssets() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            try (var stream = Files.list(getResourceDirPath("/thumbnails"))) {
                stream.filter(Files::isRegularFile)
                      .forEach(path -> {
                          var fileName = path.getFileName().toString();
                          THUMBNAIL_ASSETS.put(fileName, executor.submit(() -> loadImage(path)));
                      });
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load assets", e);
        }
    }

    public static BufferedImage getThumbnail(String fileName) {
        try {
            var future = THUMBNAIL_ASSETS.get(fileName);

            if (future == null) {
                return null;
            }
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Failed to get thumbnail", e);
            return null;
        }
    }

    private static Path getResourceDirPath(String directoryName) {
        var directoryUrl = AssetLoader.class.getResource(directoryName);
        if (directoryUrl == null) {
            throw new IllegalArgumentException("Could not find resource directory: " + directoryName);
        }
        try {
            return Path.of(directoryUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedImage loadImage(Path path) {
        LOGGER.debug("Loading image from {}", path);
        try (var stream = Files.newInputStream(path)) {
            return ImageIO.read(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
