package net.alterorb.launcher;

import com.squareup.moshi.Moshi;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Module
public class LauncherModule {

    @Provides
    @Singleton
    @Named("shared")
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
