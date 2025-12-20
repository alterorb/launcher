package net.alterorb.launcher.event.ui;

import net.alterorb.launcher.model.GameManifest;
import net.alterorb.launcher.event.Event;

public record LaunchGameEvent(GameManifest manifest) implements Event {

}
