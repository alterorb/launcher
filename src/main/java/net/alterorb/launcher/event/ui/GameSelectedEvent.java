package net.alterorb.launcher.event.ui;

import net.alterorb.launcher.alterorb.GameManifest;
import net.alterorb.launcher.event.Event;

public record GameSelectedEvent(
        GameManifest game
) implements Event {

}
