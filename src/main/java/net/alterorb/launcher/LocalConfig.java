package net.alterorb.launcher;

import java.util.Collections;
import java.util.List;

public class LocalConfig {

    public static final String DEFAULT_BRANCH = "release";

    public static final LocalConfig DEFAULT = new LocalConfig(1.0f, DEFAULT_BRANCH, List.of(DEFAULT_BRANCH));

    private float uiScalingFactor;

    private String activeBranch;

    private final List<String> branches;

    public LocalConfig(float uiScalingFactor, String activeBranch, List<String> branches) {
        this.uiScalingFactor = uiScalingFactor;
        this.activeBranch = activeBranch;
        this.branches = branches;
    }

    public static boolean isInvalid(LocalConfig config) {
        if (config.uiScalingFactor < 1.0f || config.uiScalingFactor > 4.0f) {
            return true;
        }
        var activeBranch = config.activeBranch;

        if (activeBranch == null || activeBranch.isBlank()) {
            return true;
        }
        var branches = config.branches;

        return branches == null || branches.isEmpty() || !branches.contains(DEFAULT_BRANCH) || !branches.contains(activeBranch);
    }

    public float uiScalingFactor() {
        return uiScalingFactor;
    }

    public void setUiScalingFactor(float uiScalingFactor) {
        this.uiScalingFactor = uiScalingFactor;
    }

    public String activeBranch() {
        return activeBranch;
    }

    public void setActiveBranch(String activeBranch) {
        this.activeBranch = activeBranch;
    }

    public List<String> branches() {
        return Collections.unmodifiableList(branches);
    }
}
