package net.alterorb.launcher.ui;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.Dimension;

public class GameFrameView extends JFrame {

    private static final Dimension MINIMUM_SIZE = new Dimension(640 + 16, 480 + 39);

    GameFrameView(GameFrameController gameFrameController) {
        setTitle("AlterOrb");
        setMinimumSize(MINIMUM_SIZE);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(gameFrameController);
        setLocationRelativeTo(null);
        setResizable(true);
    }
}
