package net.alterorb.launcher.ui;

import net.alterorb.launcher.alterorb.AlterorbGameConfig;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.Dimension;

public class GameFrameView extends JFrame {

    private static final Dimension MINIMUM_SIZE = new Dimension(640 + 16, 480 + 39);

    public GameFrameView(AlterorbGameConfig gameConfig) {
        this.setTitle("AlterOrb - " + gameConfig.getName());
        this.setMinimumSize(MINIMUM_SIZE);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
