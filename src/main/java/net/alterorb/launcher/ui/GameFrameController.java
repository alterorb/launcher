package net.alterorb.launcher.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.applet.Applet;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class GameFrameController extends WindowAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameFrameController.class);

    private static final GameFrameController INSTANCE = new GameFrameController();

    private final GameFrameView gameFrameView = new GameFrameView(this);

    private GameFrameController() {
    }

    public static GameFrameController instance() {
        return INSTANCE;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        LOGGER.debug("Game frame window is closing");
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
