package net.alterorb.launcher.alterorb;

public record GameManifest(
        String name,
        String thumbnail,
        String mainClass,
        String gamepack,
        String gamepackHash,
        int gamecrc,
        String serverHost,
        int serverPort
) {

}
