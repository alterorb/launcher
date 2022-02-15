package net.alterorb.launcher.alterorb;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public record AlterOrbGame(
        String name,
        String internalName,
        String mainClass,
        String gamepackHash,
        int gamecrc
) {

    public static class AlterOrbGameAdapter implements JsonDeserializer<AlterOrbGame> {

        @Override
        public AlterOrbGame deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            var jsonObject = json.getAsJsonObject();

            var name = jsonObject.get("name").getAsString();
            var internalName = jsonObject.get("internalName").getAsString();
            var mainClass = jsonObject.get("mainClass").getAsString();
            var gamepackHash = jsonObject.get("gamepackHash").getAsString();
            var gamecrc = jsonObject.get("gamecrc").getAsInt();

            return new AlterOrbGame(name, internalName, mainClass, gamepackHash, gamecrc);
        }
    }
}
