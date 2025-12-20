package net.alterorb.launcher.model;

public record GameManifest(
        String name,
        String thumbnail,
        String mainClass,
        String gamepack,
        String gamepackHash,
        int gamecrc,
        String serverHost,
        int serverPort,
        boolean allowResizing
) {

}
