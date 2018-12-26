package net.alterorb.launcher.alterorb;

import lombok.Getter;

import java.util.Map;

@Getter
public class AlterorbGameConfig {

    private String name;
    private String mainClass;
    private String gameshellClass;
    private String checkhostMethod;
    private String checkhostMethodDesc;
    private String baseUrl;
    private Map<String, String> parameters;
}
