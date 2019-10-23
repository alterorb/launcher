package net.alterorb.launcher;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.squareup.moshi.Moshi;
import okhttp3.OkHttpClient;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LauncherModule extends AbstractModule {

    @Override
    protected void configure() {
        HashMap<String, String> properties = new HashMap<>();

        properties.put("baseUrl", "https://static.alterorb.net/launcher/");
        properties.put("version", "1.9");

        Names.bindProperties(binder(), properties);
    }

    @Provides
    @Singleton
    @Named("singleThread")
    public ExecutorService provideSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient() {
        return new OkHttpClient();
    }

    @Provides
    @Singleton
    public Moshi provideMoshi() {
        return new Moshi.Builder().build();
    }
}
