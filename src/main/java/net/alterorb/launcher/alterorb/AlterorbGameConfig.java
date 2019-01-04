package net.alterorb.launcher.alterorb;

import lombok.Getter;
import net.alterorb.launcher.patcher.Patch;

import java.util.List;
import java.util.Map;

@Getter
public class AlterorbGameConfig {

    private String name;
    private String mainClass;
    private String baseUrl;
    private Map<String, String> parameters;
    private List<Patch> patches;
}
