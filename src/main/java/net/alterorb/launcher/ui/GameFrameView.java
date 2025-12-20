package net.alterorb.launcher.ui;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.Dimension;

public class GameFrameView extends JFrame {

    public static final Dimension APPLET_MIN_SIZE = new Dimension(640, 480);
    public static final Dimension APPLET_MAX_SIZE = new Dimension(1024, 1024);

    GameFrameView(GameFrameController gameFrameController) {
        setTitle("AlterOrb");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(gameFrameController);
    }
}
