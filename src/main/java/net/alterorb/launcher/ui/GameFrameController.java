package net.alterorb.launcher.ui;

import dagger.Lazy;
import net.alterorb.launcher.Launcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.applet.Applet;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Singleton
public class GameFrameController extends WindowAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameFrameController.class);

    private final GameFrameView gameFrameView = new GameFrameView(this);
    private final Lazy<Launcher> launcher;

    @Inject
    public GameFrameController(Lazy<Launcher> launcher) {
        this.launcher = launcher;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        LOGGER.trace("Game frame window is closing");
        launcher.get().shutdown();
    }

    public void display() {
        gameFrameView.setVisible(true);
    }

    public void dispose() {
        gameFrameView.dispose();
    }

    public void addApplet(Applet applet) {
        gameFrameView.add(applet);
    }
}
