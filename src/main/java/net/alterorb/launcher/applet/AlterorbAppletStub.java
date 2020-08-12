package net.alterorb.launcher.applet;

import net.alterorb.launcher.alterorb.AlterorbGame;

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class AlterorbAppletStub implements AppletStub {

    private final AlterorbAppletContext context;
    private final Map<String, String> parameters;
    private final URL documentBase;
    private final URL codeBase;

    public AlterorbAppletStub(AlterorbGame gameConfig, AlterorbAppletContext context, String documentBase, String codeBase) throws MalformedURLException {
        this.context = context;
        this.parameters = gameConfig.getParameters();

        this.documentBase = new URL(documentBase != null ? documentBase : gameConfig.getBaseUrl());
        this.codeBase = new URL(codeBase != null ? codeBase : gameConfig.getBaseUrl());
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
        return context;
    }

    @Override
    public void appletResize(int width, int height) {
    }
}
