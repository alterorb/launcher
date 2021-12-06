package net.alterorb.launcher.event.ui;

import net.alterorb.launcher.alterorb.AlterOrbGame;
import net.alterorb.launcher.event.Event;

public record GameSelectedEvent(
        AlterOrbGame game
) implements Event {

}
