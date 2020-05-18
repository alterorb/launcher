package net.alterorb.launcher;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Objects;

@Slf4j
@Singleton
public class VersionCheck {

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
