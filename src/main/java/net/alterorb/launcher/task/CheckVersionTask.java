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
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
                    int selectedOption = JOptionPane.showOptionDialog(null, "A new version of the launcher is available at https://alterorb.net/. Would you like to download it now?",
                            "Launcher Update Available", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] {"Yes", "No"}, "Yes");

                    if (selectedOption == 0) { // Yes

                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            try {
                                Desktop.getDesktop().browse(new URI("https://alterorb.net/"));
                            } catch (URISyntaxException e) {
                                LOGGER.catching(e);
                                browsingNotAvailableMessage();
                            }
                        } else {
                            browsingNotAvailableMessage();
                        }
                    }
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

    private void browsingNotAvailableMessage() {
        JOptionPane.showMessageDialog(null, "Browsing is not supported on your platform, please go to https://alterorb.net/ manually.", "Browsing Unavailable", JOptionPane.PLAIN_MESSAGE);
    }
}
