package net.alterorb.launcher.task;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import lombok.extern.log4j.Log4j2;
import net.alterorb.launcher.alterorb.AlterorbGame;
import net.alterorb.launcher.ui.LauncherView;
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
import java.util.List;

@Log4j2
@Singleton
public class FetchGameListTask implements Runnable {

    @Inject
    @Named("baseUrl")
    private String baseUrl;

    @Inject
    private OkHttpClient okHttpClient;

    @Inject
    private Moshi moshi;

    @Inject
    private LauncherView launcherView;

    @Override
    public void run() {
        LOGGER.trace("enter");
        try {
            Request request = new Builder()
                    .url(baseUrl + "available-games.json")
                    .build();

            Response response = okHttpClient.newCall(request)
                                            .execute();

            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to fetch game list");
            }
            try (ResponseBody responseBody = response.body()) {

                if (responseBody == null) {
                    throw new IOException("Empty response body");
                }

                JsonAdapter<List<AlterorbGame>> jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, AlterorbGame.class));

                List<AlterorbGame> availableGames = jsonAdapter.fromJson(responseBody.source());
                LOGGER.debug("Available games={}", availableGames);
                launcherView.setGameList(availableGames);
            }
        } catch (IOException e) {
            LOGGER.catching(e);
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        LOGGER.trace("exit");
    }
}
