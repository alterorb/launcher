package net.alterorb.launcher.task;

import lombok.extern.log4j.Log4j2;
import net.alterorb.launcher.ProgressListenableSource;
import net.alterorb.launcher.ProgressListenableSource.ProgressListener;
import net.alterorb.launcher.StorageManager;
import net.alterorb.launcher.alterorb.AlterorbGame;
import net.alterorb.launcher.ui.LauncherView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.JOptionPane;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

@Log4j2
@Singleton
public class DownloadGamepackTask implements Runnable {

    @Inject
    @Named("baseUrl")
    private String baseUrl;

    @Inject
    private OkHttpClient okHttpClient;

    @Inject
    private StorageManager storageManager;

    @Inject
    private LauncherView launcherView;

    @Inject
    @Named("singleThread")
    private ExecutorService executorService;

    @Inject
    private LaunchGameTask launchGameTask;

    @Override
    public void run() {
        LOGGER.trace("enter");
        try {
            AlterorbGame alterorbGame = launcherView.getSelectedGame();

            Request request = new Builder()
                    .url(baseUrl + "gamepacks/" + alterorbGame.getInternalName() + ".jar")
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
                    launcherView.updateProgressBar((int) percentage);
                };
                launcherView.updateProgressBarText("Downloading gamepack...");

                try (BufferedSink sink = Okio.buffer(Okio.sink(gamepackPath));
                        ProgressListenableSource source = new ProgressListenableSource(responseBody.source(), responseBody.contentLength(), progressListener)) {
                    sink.writeAll(source);
                }
            }
            executorService.submit(launchGameTask);
        } catch (IOException e) {
            LOGGER.catching(e);
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        LOGGER.trace("exit");
    }
}
