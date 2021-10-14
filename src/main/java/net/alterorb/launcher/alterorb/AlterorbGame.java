package net.alterorb.launcher.alterorb;

import net.alterorb.launcher.patcher.Patch;

import java.util.List;
import java.util.Map;

public record AlterorbGame(
        String name,
        String internalName,
        String mainClass,
        String baseUrl,
        String gamepackHash,
        Map<String, String> parameters,
        List<Patch> patches
) {

}
