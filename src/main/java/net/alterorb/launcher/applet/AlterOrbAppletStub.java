package net.alterorb.launcher.applet;

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AlterOrbAppletStub implements AppletStub {

    private static final Map<String, String> STATIC_PARAMS = Map.of(
            "overxgames", "45",
            "overxachievements", "1000",
            "member", "no",
            "gameport1", "43594",
            "gameport2", "43594",
            "servernum", "8003"
    );

    private final AlterOrbAppletContext context;
    private final Map<String, String> parameters;
    private final URL documentBase;
    private final URL codeBase;

    private AlterOrbAppletStub(AlterOrbAppletContext context, Map<String, String> parameters, URL documentBase, URL codeBase) {
        this.context = context;
        this.parameters = parameters;
        this.documentBase = documentBase;
        this.codeBase = codeBase;
    }

    public static AlterOrbAppletStub from(AlterOrbAppletContext context, Map<String, String> params, String documentBase, String codeBase) throws MalformedURLException {
        Map<String, String> parameters = new HashMap<>();
        parameters.putAll(STATIC_PARAMS);
        parameters.putAll(params);

        var documentBaseUrl = URI.create(documentBase).toURL();
        var codeBaseUrl = URI.create(codeBase).toURL();

        return new AlterOrbAppletStub(context, parameters, documentBaseUrl, codeBaseUrl);
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
