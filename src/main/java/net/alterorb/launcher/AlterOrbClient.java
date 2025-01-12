package net.alterorb.launcher;

import com.google.gson.Gson;
import com.vdurmont.semver4j.Semver;
import net.alterorb.launcher.alterorb.BranchManifest;
import net.alterorb.launcher.alterorb.GameManifest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.IntConsumer;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public class AlterOrbClient {

    private static final URI BASE_URL = URI.create("https://static.alterorb.net/launcher/v4/");

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
                                                            .connectTimeout(Duration.ofSeconds(10))
                                                            .build();

    private static final Gson GSON = new Gson();

    public Semver fetchVersion() throws IOException, InterruptedException {
        var uri = BASE_URL.resolve("version.json");
        var request = HttpRequest.newBuilder(uri)
                                 .header("User-Agent", "AlterOrb " + Launcher.VERSION)
                                 .build();

        var response = HTTP_CLIENT.send(request, BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Unexpected response code " + response.statusCode() + " from " + uri);
        }
        return new Semver(response.body());
    }

    public BranchManifest fetchBranchManifest(String branch) throws IOException, InterruptedException {
        var uri = BASE_URL.resolve("branches/" + branch + "/").resolve("manifest.json");

        var request = HttpRequest.newBuilder(uri)
                                 .header("User-Agent", "AlterOrb " + Launcher.VERSION)
                                 .build();

        var response = HTTP_CLIENT.send(request, BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Unexpected response code " + response.statusCode() + " from " + uri);
        }
        return GSON.fromJson(response.body(), BranchManifest.class);
    }

    public void downloadGamepack(GameManifest manifest, Path directory, IntConsumer progressListener) throws IOException, InterruptedException {
        var uri = BASE_URL.resolve("gamepacks").resolve(manifest.gamepack());
        var request = HttpRequest.newBuilder(uri)
                                 .header("User-Agent", "AlterOrb " + Launcher.VERSION)
                                 .build();

        var response = HTTP_CLIENT.send(request, BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("Unexpected response code " + response.statusCode() + " from " + uri);
        }

        long contentLength = response.headers()
                                     .firstValue("Content-Length")
                                     .map(Long::parseLong)
                                     .orElse(-1L);

        var buffer = new byte[4096];
        var bytesRead = 0;
        long totalBytes = 0;

        var gamepackPath = directory.resolve(manifest.gamepack());

        try (var stream = response.body();
                var outputStream = Files.newOutputStream(gamepackPath, CREATE, WRITE, TRUNCATE_EXISTING)) {
            while ((bytesRead = stream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;

                if (contentLength != -1) {
                    var progress = totalBytes * 100 / contentLength;
                    progressListener.accept((int) progress);
                }
            }
        }
    }
}
