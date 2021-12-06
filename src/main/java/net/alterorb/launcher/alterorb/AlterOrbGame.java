package net.alterorb.launcher.alterorb;

public record AlterOrbGame(
        String name,
        String internalName,
        String mainClass,
        String gamepackHash,
        int gamecrc
) {

}
