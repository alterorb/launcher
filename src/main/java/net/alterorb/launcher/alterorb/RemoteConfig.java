package net.alterorb.launcher.alterorb;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.List;

public record RemoteConfig(
        String version,
        String server,
        List<AlterOrbGame> games
) {

    public static class RemoteConfigAdapter implements JsonDeserializer<RemoteConfig> {

        @Override
        public RemoteConfig deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            var jsonObject = json.getAsJsonObject();
            var version = jsonObject.get("version").getAsString();
            var server = jsonObject.get("server").getAsString();
            AlterOrbGame[] games = ctx.deserialize(jsonObject.getAsJsonArray("games"), AlterOrbGame[].class);

            return new RemoteConfig(version, server, List.of(games));
        }
    }
}
