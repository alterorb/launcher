package net.alterorb.launcher.alterorb;

import lombok.Getter;
import net.alterorb.launcher.patcher.Patch;

import java.util.List;
import java.util.Map;

@Getter
public class AlterorbGame {

    private String name;
    private String internalName;
    private String mainClass;
    private String baseUrl;
    private String gamepackHash;
    private Map<String, String> parameters;
    private List<Patch> patches;
}
