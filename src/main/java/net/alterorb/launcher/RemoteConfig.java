package net.alterorb.launcher;

import net.alterorb.launcher.alterorb.AlterOrbGame;

import java.util.List;

public record RemoteConfig(
        String version,
        String server,
        List<AlterOrbGame> games
) {

}
