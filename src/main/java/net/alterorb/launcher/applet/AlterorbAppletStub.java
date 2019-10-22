package net.alterorb.launcher.applet;

import net.alterorb.launcher.alterorb.AlterorbGameConfig;

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class AlterorbAppletStub implements AppletStub {

    private static final AlterorbAppletContext APPLET_CONTEXT = new AlterorbAppletContext();
    private final Map<String, String> parameters;
    private final URL documentBase;
    private final URL codeBase;

    public AlterorbAppletStub(AlterorbGameConfig gameConfig) throws MalformedURLException {
        this.parameters = gameConfig.getParameters();

        String sysDocumentBase = System.getenv("DOCUMENT_BASE");
        String sysCodeBase = System.getenv("CODE_BASE");

        this.documentBase = new URL(sysDocumentBase != null ? sysDocumentBase : gameConfig.getBaseUrl());
        this.codeBase = new URL(sysCodeBase != null ? sysCodeBase : gameConfig.getBaseUrl());
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public URL getDocumentBase() {
        return documentBase;
    }

    @Override
    public URL getCodeBase() {
        return codeBase;
    }

    @Override
    public String getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public AppletContext getAppletContext() {
        return APPLET_CONTEXT;
    }

    @Override
    public void appletResize(int width, int height) {
    }
}
