package net.alterorb.launcher;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Objects;

@Singleton
public class VersionCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionCheck.class);

    private final OkHttpClient okHttpClient;
    private final Moshi moshi;

    @Inject
    public VersionCheck(OkHttpClient okHttpClient, Moshi moshi) {
        this.okHttpClient = okHttpClient;
        this.moshi = moshi;
    }

    public boolean check() throws IOException {
        Request request = new Builder()
                .url(Launcher.BASE_URL + "version.json")
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

            return Objects.equals(Launcher.VERSION, remoteVersion);
        }
    }
}
