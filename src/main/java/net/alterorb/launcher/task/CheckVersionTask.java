package net.alterorb.launcher.task;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import lombok.extern.log4j.Log4j2;
import net.alterorb.launcher.ui.controller.LauncherController;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.JOptionPane;
import java.io.IOException;
import java.util.Objects;

@Log4j2
@Singleton
public class CheckVersionTask implements Runnable {

    @Inject
    @Named("baseUrl")
    private String baseUrl;

    @Inject
    @Named("version")
    private String version;

    @Inject
    private OkHttpClient okHttpClient;

    @Inject
    private Moshi moshi;

    @Inject
    private LauncherController launcherController;

    @Override
    public void run() {
        LOGGER.trace("enter");
        try {
            Request request = new Builder()
                    .url(baseUrl + "version.json")
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

                if (!Objects.equals(version, remoteVersion)) {
                    JOptionPane.showMessageDialog(null, "A new version of the launcher is available", "Update Available", JOptionPane.PLAIN_MESSAGE);
                } else {
                    launcherController.display();
                }
            }
        } catch (IOException e) {
            LOGGER.catching(e);
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        LOGGER.trace("exit");
    }
}
