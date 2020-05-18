package net.alterorb.launcher.applet;

import lombok.extern.slf4j.Slf4j;
import net.alterorb.launcher.Launcher;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AudioClip;
import java.awt.Image;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;

@Slf4j
public class AlterorbAppletContext implements AppletContext {

    private static final String QUIT_APPLET_PATH = "/quit.ws";

    private final Launcher launcher;

    public AlterorbAppletContext(Launcher launcher) {
        this.launcher = launcher;
    }

    @Override
    public AudioClip getAudioClip(URL url) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public Image getImage(URL url) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public Applet getApplet(String name) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public Enumeration<Applet> getApplets() {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public void showDocument(URL url) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public void showDocument(URL url, String target) {

        if (url != null && Objects.equals(url.getPath(), QUIT_APPLET_PATH)) {
            LOGGER.trace("Applet requested shutdown");
            launcher.shutdown();
        }
    }

    @Override
    public void showStatus(String status) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public void setStream(String key, InputStream stream) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public InputStream getStream(String key) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public Iterator<String> getStreamKeys() {
        throw new UnsupportedOperationException("Unsupported operation");
    }
}
