package net.alterorb.launcher.event.ui;

import net.alterorb.launcher.alterorb.AlterOrbGame;
import net.alterorb.launcher.event.Event;

public record LaunchGameEvent(AlterOrbGame game) implements Event {

}
